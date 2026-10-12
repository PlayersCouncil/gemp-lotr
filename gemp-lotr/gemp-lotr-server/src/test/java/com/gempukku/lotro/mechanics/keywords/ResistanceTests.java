package com.gempukku.lotro.mechanics.keywords;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Resistance and burdens during play.
 *
 * Comprehensive Rules 4.2, "resistance": "Companions preceding the Shadows expansion set (other than versions of
 * Frodo or Sam) do not have their resistance printed on the card.  These companions have a resistance of 6.  Allies
 * have a resistance of zero.  Each burden on your Ring-bearer reduces the resistance of every companion in your
 * fellowship by 1.  If your Ring-bearer's resistance is reduced to zero, he is corrupted, and you lose the game.  If
 * the resistance of any of your other companions is reduced to zero, there is no immediate penalty".  "Attribute
 * bonus": a borne card's "positive modifier for a character's strength, vitality, and/or resistance".
 *
 * This class is what lets a card's own tests take a printed resistance, and a printed resistance bonus, as read
 * (Ketura on Q-0279, 2026-10-12).
 *
 * What is in another class and is not gone over again here:
 *   corruption by the bid, before the game: CorruptionBeforeTheGameStartsTests;
 *   the burdens when the Ring passes to a new Ring-bearer: RingBearerSuccessionTests;
 *   a hindered Ring-bearer, whose corruption is not judged while he is hindered: HinderedRingBearerTests;
 *   a resistance bonus coming and going with the item: BorneItemsTests.
 *
 * The burdens of a test are the bid's, taken off by StartGame(), and then those the test adds, as a cheat to set
 * the board or by Light Shining Faintly 4_309, a [shire] Fellowship event of cost 0: "Add a burden to heal a
 * Ring-bound companion twice."  The Ring-bearer is Ring-bound by rule, so it can always be played.
 */
public class ResistanceTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.RulingRing);
	}

	protected VirtualTableScenario GetScenario(String ring) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: companion, resistance 5 printed.  A fellowship ability that removes a
					// burden, never used, and a response to Frodo's death.
					put("sam", "1_311");
					// Gondorian Knight: companion of the Return of the King set, with no resistance printed (the rules
					// give him 6); "Knight." is his whole text
					put("knight", "5_35");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: companion, resistance 6; his text is about tales
					// Erland, Advisor to Brand: an ally.  "Shadow players may not look at or reveal cards in your hand."
					put("erland", "2_21");
					// Salt from the Shire: a box, resistance +2.  "Bearer must be a Hobbit."  Its abilities are never used.
					put("salt", "11_171");
					// Our Foes Are Weak: a Shadow condition, strength -1, resistance -3.  "Bearer must be an unbound
					// companion.  Limit 1 per bearer."  Nothing else.
					put("foes", "11_196");
					put("light", "4_309");      // Light Shining Faintly, as above
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				ring
		);
	}

	@Test
	public void ACompanionHasHisPrintedResistanceOr6WhereNoneIsPrintedAndAnAllyHasNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var knight = scn.GetFreepsCard("knight");
		var erland = scn.GetFreepsCard("erland");
		scn.MoveCompanionsToTable(sam, knight);
		scn.MoveAlliesToTable(erland);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(knight));
		assertEquals(0, scn.GetResistance(erland));
	}

	@Test
	public void EachBurdenTakesOneFromEveryCompanionAndNothingFromAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var knight = scn.GetFreepsCard("knight");
		var erland = scn.GetFreepsCard("erland");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(sam, knight);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(4, scn.GetResistance(knight));
		assertEquals(0, scn.GetResistance(erland));

		// One more, added by a card.
		scn.FreepsPlayCard(light);

		assertEquals(3, scn.GetBurdens());
		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(2, scn.GetResistance(sam));
		assertEquals(3, scn.GetResistance(knight));
		assertEquals(0, scn.GetResistance(erland));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRingBearerAtZeroResistanceIsCorruptedAndHisPlayerLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var light = scn.GetFreepsCard("light");
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddBurdens(9);

		assertEquals(1, scn.GetResistance(frodo));
		assertFalse(scn.GameIsFinished());

		scn.FreepsPlayCard(light);

		assertEquals(10, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(frodo));
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
		assertTrue(scn.ShadowHasWon());
	}

	@Test
	public void AnotherCompanionAtZeroResistanceIsNotCorrupted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddBurdens(4);

		assertEquals(1, scn.GetResistance(sam));

		scn.FreepsPlayCard(light);

		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetResistance(sam));
		assertEquals(5, scn.GetResistance(frodo));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AResistanceBonusOrMinusPrintedOnABorneCardCountsAndResistanceIsNeverBelowZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var salt = scn.GetFreepsCard("salt");
		var foes = scn.GetShadowCard("foes");
		scn.MoveCompanionsToTable(pippin, knight);
		scn.AttachCardsTo(knight, foes);
		scn.MoveCardsToHand(salt);

		scn.StartGame();

		// 6 - 3.
		assertEquals(3, scn.GetResistance(knight));
		assertEquals(6, scn.GetResistance(pippin));

		scn.FreepsPlayCard(salt);
		scn.FreepsChooseCard(pippin);
		assertAttachedTo(salt, pippin);
		assertEquals(8, scn.GetResistance(pippin));

		// Four burdens: 8 - 4, and 3 - 4 read as 0.
		scn.AddBurdens(4);
		assertEquals(4, scn.GetResistance(pippin));
		assertEquals(0, scn.GetResistance(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOneRingsPrintedResistanceCountsForTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		// The One Ring, The Ring of Rings 11_1: resistance +2.  Its response answers a wound, and none is taken here.
		var scn = GetScenario("11_1");

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(light);

		scn.StartGame();

		assertEquals(12, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));

		// Ten burdens would corrupt a Frodo of 10; this one is left 1 and lives.
		scn.AddBurdens(9);
		scn.FreepsPlayCard(light);

		assertEquals(10, scn.GetBurdens());
		assertEquals(2, scn.GetResistance(frodo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
