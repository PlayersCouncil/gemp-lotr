package com.gempukku.lotro.hall;

import com.gempukku.lotro.db.PlayerDAO;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;

/** GET /hall/players: registered names by prefix for the invite picker. */
public class PlayerNameSearchTest {
    private static PlayerDAO dao(List<String> rows) {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Mockito.when(dao.findPlayerNames(Mockito.anyString(), Mockito.anyInt())).thenReturn(rows);
        return dao;
    }

    @Test
    public void onlyPrefixMatchesAndNeverTheSearcher() {
        PlayerDAO dao = dao(List.of("sam", "Samwise", "sammy", "BigSam", "MrSam"));
        assertEquals(List.of("Samwise", "sammy"), PlayerNameSearch.byPrefix(dao, "Sa", "sam", 10));
        Mockito.verify(dao).findPlayerNames("Sa", 11);
    }

    @Test
    public void tooShortAPrefixSearchesNothing() {
        PlayerDAO dao = dao(List.of("sam"));
        assertTrue(PlayerNameSearch.byPrefix(dao, "s", "bob", 10).isEmpty());
        assertTrue(PlayerNameSearch.byPrefix(dao, " s ", "bob", 10).isEmpty());
        assertTrue(PlayerNameSearch.byPrefix(dao, null, "bob", 10).isEmpty());
        Mockito.verify(dao, Mockito.never()).findPlayerNames(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void atMostTenNames() {
        PlayerDAO dao = dao(List.of("ab1", "ab2", "ab3", "ab4", "ab5", "ab6", "ab7", "ab8", "ab9", "ab10", "ab11", "ab12"));
        assertEquals(10, PlayerNameSearch.byPrefix(dao, "ab", "bob", 50).size());
        assertEquals(3, PlayerNameSearch.byPrefix(dao, "ab", "bob", 3).size());
        assertEquals(10, PlayerNameSearch.clampLimit("500"));
        assertEquals(1, PlayerNameSearch.clampLimit("0"));
        assertEquals(10, PlayerNameSearch.clampLimit("junk"));
        assertEquals(10, PlayerNameSearch.clampLimit(null));
        assertEquals(5, PlayerNameSearch.clampLimit(" 5 "));
    }

    @Test
    public void likeWildcardsAreTheDaosJob() {
        // the prefix goes to the DAO as typed (it escapes % and _ in its parameterised LIKE); "_" in names is common
        PlayerDAO dao = dao(List.of("a_b", "a_bc"));
        assertEquals(List.of("a_b", "a_bc"), PlayerNameSearch.byPrefix(dao, "a_", "bob", 10));
        Mockito.verify(dao).findPlayerNames("a_", 11);
    }
}
