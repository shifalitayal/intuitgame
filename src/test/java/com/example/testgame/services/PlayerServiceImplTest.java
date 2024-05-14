package com.example.testgame.services;

import com.example.testgame.exceptions.NegativePlayerIdOrPlayerNullException;
import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceImplTest {

    @Mock
    PlayerRepository playerRepository;

    @InjectMocks
    PlayerServiceImpl playerService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testAddPlayerWithValidPlayer() throws NegativePlayerIdOrPlayerNullException {

        Player player = new Player(1, "John Doe");
        when(playerRepository.save(player)).thenReturn(player);

        Player result = playerService.addPlayer(player);

        assertEquals(player, result);
        verify(playerRepository, times(1)).save(player);
    }

    // Add more positive test cases for valid players

    @Test
    void testAddPlayerWithNullPlayer() {


        assertThrows(NegativePlayerIdOrPlayerNullException.class, () -> playerService.addPlayer(null));

        verifyNoInteractions(playerRepository);
    }

    @Test
    void testAddPlayerWithNegativePlayerId() {


        assertThrows(NegativePlayerIdOrPlayerNullException.class, () -> playerService.addPlayer(new Player(-1,"John")));

        verifyNoInteractions(playerRepository);
    }


}