package cz.cvut.fel.pjv.managers;


import cz.cvut.fel.pjv.*;
import cz.cvut.fel.pjv.entities.Camera;
import cz.cvut.fel.pjv.entities.Entity;
import cz.cvut.fel.pjv.entities.Player;
import cz.cvut.fel.pjv.entities.TransmittedPlayer;
import cz.cvut.fel.pjv.weapons.MeleeHitBox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.Game.*;



/**
 * The NetworkManager class is responsible for setting up and maintaining the
 * connection between the server (host) and client in a multiplayer game.
 *
 * <p>It handles sending and receiving game state packets, synchronizing entities,
 * and processing various network events, such as sound sharing, pauses, and deletion requests.</p>
 */
public class NetworkManager {
    private static final Logger logger = Logger.getLogger("NetworkManager");
    private String ip = "localhost";
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final ConcurrentLinkedQueue<ArrayList<Entity>> receiveQueue = new ConcurrentLinkedQueue<>();
    private final BlockingQueue<ArrayList<Entity>> sendQueue = new LinkedBlockingQueue<>();


    /**
     * Synchronizes game entities between the Server and Client.
     *
     * <p>This method handles network synchronization differently based on whether the current
     * instance is a host or a client. It sends the client a snapshot of current entities and processes
     * incoming entity updates, managing various types of events (e.g., pause, sound, deletion).</p>
     */
    public void synchronizeMultiplayer() {
        /// Multiplayer Host
        if (multiplayer && host && connectionSetUp) {
            sendQueue.offer(new ArrayList<>(entities));
            for(Entity entity : entities) {
                entity.setTransmitted(true);
            }
            sendQueue.offer(new ArrayList<>(exportEntities));
            exportEntities.clear();

            while (!receiveQueue.isEmpty()) {
                ArrayList<Entity> receivedEntities = receiveQueue.poll();
                for (Entity entity : receivedEntities) {
                    if (entity instanceof Player) continue;
                    if (entity instanceof Camera) continue;
                    if (entity instanceof PauseRequest){
                        paused = true;
                        continue;
                    }
                    if (entity instanceof UnPauseRequest){
                        paused = false;
                        continue;
                    }
                    if(entity instanceof ShareSound){
                        soundManager.playSound(((ShareSound) entity).sound);
                        continue;
                    }
                    if(entity instanceof DeleteRequest)
                    {
                        for (Entity entityIterator : entities) {
                            if (entityIterator.id == ((DeleteRequest) entity).deleteId) {
                                entityIterator.destroy();
                            }
                        }
                        continue;
                    }

                    boolean match = false;
                    for (Entity entityIterator : entities) {
                        if (entityIterator.id == entity.id) {
                            entityIterator.setX(entity.getX());
                            entityIterator.setY(entity.getY());
                            entityIterator.setState(entity.getState());
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        entity.createNewDrawer();
                        entity.rotate(entity.getRotationAngle());
                        entity.createCollisionSet();
                        if(entity instanceof MeleeHitBox){
                            entity.drawer.getBlockImageView().setOpacity(((MeleeHitBox) entity).opacity);
                        }
                        if (entity instanceof TransmittedPlayer && entity.id == Long.MIN_VALUE){
                            players.add(entity);
                        }
                        else  entity.id = entityIdCounter++;
                        entities.add(entity);
                    }
                }
            }
        }
        /// Multiplayer Client
        if (multiplayer && !host && connectionSetUp) {
            while (!receiveQueue.isEmpty()) {
                ArrayList<Entity> receivedEntities = receiveQueue.poll();
                for (Entity entity : receivedEntities) {
                    if (entity instanceof Player) continue;
                    if (entity instanceof Camera) continue;
                    if (entity instanceof PauseRequest){
                        paused = true;
                        continue;
                    }
                    if (entity instanceof UnPauseRequest){
                        paused = false;
                        continue;
                    }
                    if(entity instanceof ShareSound){
                        soundManager.playSound(((ShareSound) entity).sound);
                        continue;
                    }
                    if (entity instanceof TransmittedPlayer && entity.id == Long.MIN_VALUE)
                    {
                        if(player.getHealth() != entity.getHealth())
                        {
                            player.subtractHealth(player.getHealth() - entity.getHealth());
                        }
                        continue;
                    }
                    boolean match = false;
                    for (Entity entityIterator : entities) {
                        if (entityIterator.id == entity.id) {
                            entityIterator.setX(entity.getX());
                            entityIterator.setY(entity.getY());
                            entityIterator.setState(entity.getState());
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        entity.createNewDrawer();
                        entity.rotate(entity.getRotationAngle());
                        entity.createCollisionSet();
                        if(entity instanceof MeleeHitBox){
                            entity.drawer.getBlockImageView().setOpacity(((MeleeHitBox) entity).opacity);
                        }
                        entities.add(entity);
                    }
                }
            }

            exportEntities.add(player.transmittedPlayer);
            sendQueue.offer(new ArrayList<>(exportEntities));
            exportEntities.clear();
        }
    }


    /**
     * Initializes the multiplayer connection between the Server (host) and Client.
     *
     * <p>This method sets up the required sockets and object streams for communication.
     * In host mode, it waits for a client connection, sends the level data, and starts separate
     * threads for sending and receiving game state packets. In client mode, it attempts to connect to
     * the server, receives level data, and similarly starts threads to handle outgoing and incoming data.</p>
     *
     * @throws RuntimeException if a connection or communication error occurs.
     */
    public void setUpMultiplayer() {
        /// Host
        if(host) {
            try {
                ServerSocket serverSocket = new ServerSocket(12345);
                logger.info("Server started. Waiting for client...");

                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected.");
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                out.reset();
                in = new ObjectInputStream(clientSocket.getInputStream());
                /// Send Level
                out.writeObject(level);
                out.flush();
                out.reset();
                /// Confirmation
                in.readBoolean();
                connectionSetUp = true;
                logger.info("Level transferred.");
            } catch (IOException e) {
                logger.warning("Failed to connect to client.");
                throw new RuntimeException(e);
            }
            /// Host Sending
            new Thread(() -> {
                try {
                    while (true) {
                        ArrayList<Entity> toSend = sendQueue.take();
                        out.writeObject(toSend);
                        out.flush();
                        out.reset();
                    }
                } catch (IOException | InterruptedException e) {
                    logger.warning("Failed to send data to client.");
                    e.printStackTrace();
                }
            }).start();
            /// Host Receiving
            new Thread(() -> {
                while (true) {
                    try {
                        ArrayList<Entity> received = (ArrayList<Entity>) in.readObject();
                        receiveQueue.add(received);
                    } catch (IOException | ClassNotFoundException e) {
                        logger.warning("Failed to receive data from client.");
                        e.printStackTrace();
                        break;
                    }
                }
            }).start();
        }
        /// Client
        if(!host) {
            try {
                System.setOut(new PrintStream(new FileOutputStream("client_log.txt")));
            } catch (FileNotFoundException e) {
                logger.warning("Failed to create client log file.");
                throw new RuntimeException(e);
            }
            entities.clear();
            entities.add(player);
            entities.add(camera);
            player.transmittedPlayer.drawer.setVisible(true);
            player.transmittedPlayer.id = Long.MIN_VALUE;
            player.transmittedPlayer.setMovable(true);
            player.transmittedPlayer.setCollisionImmunity(false);
            player.transmittedPlayer.setWidth(player.getWidth());
            player.transmittedPlayer.setHeight(player.getHeight());

            try {
                Socket socket = null;
                int attempts = 0;
                while (socket == null && attempts < 60) {
                    try {
                        socket = new Socket(ip, 12345);
                    } catch (IOException e) {
                        logger.info("Connection failed, retrying...");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            logger.warning("Failed to sleep.");
                            ex.printStackTrace();
                        }
                        attempts++;
                    }
                }
                logger.info("Connected to server.");
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                out.reset();
                in = new ObjectInputStream(socket.getInputStream());
                /// Receive Level
                try {
                    level = (Level) in.readObject();
                    logger.info("Level received.");
                } catch (ClassNotFoundException e) {
                    logger.severe("Failed to receive level data.");
                    throw new RuntimeException(e);
                }
                /// Send Confirmation
                out.writeBoolean(true);
                out.flush();
                out.reset();


            } catch (IOException e) {
                logger.severe("Failed to connect to server.");
                throw new RuntimeException(e);
            }
            /// Client Sending
            new Thread(() -> {
                try {
                    while (true) {
                        ArrayList<Entity> toSend = sendQueue.take(); // Waits until data is available
                        out.writeObject(toSend);
                        out.flush();
                        out.reset();
                    }
                } catch (IOException | InterruptedException e) {
                    logger.severe("Failed to send data to server.");
                    e.printStackTrace();
                }
            }).start();
            /// Client Receiving
            new Thread(() -> {
                while (true) {
                    try {
                        ArrayList<Entity> received = (ArrayList<Entity>) in.readObject();
                        receiveQueue.add(received);
                    } catch (IOException | ClassNotFoundException e) {
                        logger.warning("Failed to receive data from server.");
                        e.printStackTrace();
                        break;
                    }
                }
            }).start();
            connectionSetUp = true;
        }
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
}