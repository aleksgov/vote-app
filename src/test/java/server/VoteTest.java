package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class VoteTest {
    private Vote vote;

    @BeforeEach
    void setUp() {
        vote = new Vote("Test Title", "Test Description", List.of("Option1", "Option2"), "admin");
    }

    @Test
    void testAddVoteSuccess() {
        assertTrue(vote.addVote("Option1", "user1"));
        assertEquals(1, vote.getOptionVotes().get("Option1"));
    }

    @Test
    void testAddVoteDuplicateUser() {
        vote.addVote("Option1", "user1");
        assertFalse(vote.addVote("Option2", "user1"));
    }

    @Test
    void testAddVoteInvalidOption() {
        assertFalse(vote.addVote("InvalidOption", "user1"));
    }
}