package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * The summary of a replay (ReplayMetadata), as GameRecorder makes it when a game ends: each player's recording,
 * the events a channel joined to the game from its first step was sent, is read by ParseReplay.
 *
 * A deck's StartingFellowship is the cards of that player's put into play as characters or attached to one before
 * the game started (the first BETWEEN_TURNS phase): the Ring-bearer, The One Ring he bears, and the companions of
 * the starting fellowship.  A card the player attaches once the game is going (a possession played on a companion)
 * is not of it, any more than a companion he plays then (F-0469: the guard of the game's start held only the
 * characters, so every card attached in the whole game was counted).
 */
public class ReplayMetadataTests
{
	/**
	 * A test table that keeps a recording of each seat as GameRecorder does: a channel that is no client's
	 * connection, joined to the game before it starts.
	 */
	private static class RecordedTable extends VirtualTableScenario {
		// Filled by BeforeGameStarts, which the table's constructor calls: no initializer, which would run after it
		// and empty it again.
		private HashMap<String, GameCommunicationChannel> _recordings;

		RecordedTable(HashMap<String, String> cards) throws CardNotFoundException, DecisionResultInvalidException {
			super(cards, VirtualTableScenario.FellowshipSites, VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing);
		}

		@Override
		protected void BeforeGameStarts() {
			super.BeforeGameStarts();
			_recordings = new HashMap<>();
			for (String player : List.of(P1(), P2())) {
				var channel = new GameCommunicationChannel(player, 0, false, game().getFormat());
				game().addGameStateListener(player, channel);
				_recordings.put(player, channel);
			}
		}

		ReplayMetadata Summarize(String player) {
			var summary = new ReplayMetadata();
			summary.Decks.put(player, summary.new DeckMetadata());
			summary.ParseReplay(player, _recordings.get(player).consumeGameEvents());
			return summary;
		}
	}

	@Test
	public void ACardAttachedOnceTheGameHasStartedIsNotInTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = new RecordedTable(new HashMap<>()
		{{
			put("gimli", "1_13");       // Gimli, Son of Glóin: companion, cost 2, a Dwarf
			put("axe", "1_9");          // Dwarven Axe: possession, cost 0, bearer must be a Dwarf
			put("legolas", "1_50");     // Legolas, Greenleaf: companion, cost 2
		}});

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var legolas = scn.GetFreepsCard("legolas");

		// Gimli goes into the starting fellowship, the other player's stays empty
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(gimli);
		scn.FreepsDeclineChoosing();
		scn.ShadowDeclineChoosing();

		scn.MoveCardsToHand(axe, legolas);
		scn.StartGame(true, false);

		// In the first fellowship phase the axe is played on Gimli, and Legolas joins
		scn.FreepsPlayCard(axe);
		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.ATTACHED, axe);
		assertSame(gimli, axe.getAttachedTo());
		assertInZone(Zone.FREE_CHARACTERS, legolas);

		var fellowship = scn.Summarize(scn.P1()).Decks.get(scn.P1()).StartingFellowship;

		// Frodo, The One Ring he bears from the start, and Gimli: nothing played after the game began
		assertEquals(List.of("1_290", "1_2", "1_13").stream().sorted().toList(), fellowship.stream().sorted().toList());
	}
}
