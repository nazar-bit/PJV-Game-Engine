package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.managers.BlockManager;
import cz.cvut.fel.pjv.managers.ChunkManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class LevelGeneratorTest {

    @Test
    void generateLevel() {

        LevelGenerator.AbstractLevel abstractLevel = LevelGenerator.generateAbstractMap(5, 5 , 10);
        assertNotNull(abstractLevel);



        // Check if the level has the correct dimensions
        assertEquals(5, abstractLevel.mapOfChunks.size());
        assertEquals(5, abstractLevel.mapOfChunks.get(0).size());
        // Check if the level has the correct length of the path
        assertEquals(10, abstractLevel.path.size());

        // Check if the all doors are connected
        for(int i = 0; i < abstractLevel.mapOfChunks.size(); i++){
            for(int j = 0; j < abstractLevel.mapOfChunks.get(i).size(); j++){
                LevelGenerator.ChunkToPlace chunk = abstractLevel.mapOfChunks.get(i).get(j);
                if(chunk != null){
                    if(chunk.doors[0]){
                        assertNotNull(abstractLevel.mapOfChunks.get(i-1).get(j));
                        assertTrue(abstractLevel.mapOfChunks.get(i-1).get(j).doors[2]);
                    }
                    if(chunk.doors[1]){
                        assertNotNull(abstractLevel.mapOfChunks.get(i).get(j+1));
                        assertTrue(abstractLevel.mapOfChunks.get(i).get(j+1).doors[3]);
                    }
                    if(chunk.doors[2]){
                        assertNotNull(abstractLevel.mapOfChunks.get(i+1).get(j));
                        assertTrue(abstractLevel.mapOfChunks.get(i+1).get(j).doors[0]);
                    }
                    if(chunk.doors[3]) {
                        assertNotNull(abstractLevel.mapOfChunks.get(i).get(j - 1));
                        assertTrue(abstractLevel.mapOfChunks.get(i).get(j - 1).doors[1]);
                    }

                }
            }
        }
    }
}