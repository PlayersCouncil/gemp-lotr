package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_034_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior1", "103_34");
					put("warrior2", "103_34");
					put("deadman1", "10_27");   // Dead Man of Dunharrow: a [gondor] Wraith companion, strength 6; his text is about being played
					put("deadman2", "10_27");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion with no text, cost 2
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; valiant is his whole text
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf companion; a skirmish ability never used here
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man who is an ally; a maneuver ability only

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TormentedWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Tormented Warrior
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 6
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: Enduring.
		 * 	To play, add a threat.
		 * 	This companion is strength +1 for each companion in the dead pile.
		 * 	This companion is strength +1 for each Wraith in the dead pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior1");

		assertEquals("Tormented Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedForThreeToTheFellowshipAddingAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior1");
		scn.MoveCardsToHand(warrior);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(warrior));
		scn.FreepsPlayCard(warrior);

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, warrior);
		// Nobody is in the dead pile.
		assertEquals(6, scn.GetStrength(warrior));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileThereAreAsManyThreatsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToHand(warrior, knight);
		// Frodo is the only companion, so one threat is as many as there may be.
		scn.AddThreats(1);

		scn.StartGame();

		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsPlayAvailable(warrior));

		// A second companion makes room for a second threat.
		scn.FreepsPlayCard(knight);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(warrior));

		scn.FreepsPlayCard(warrior);
		assertEquals(2, scn.GetThreats());
		// 2 for the Knight + 3 for the Warrior
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, warrior);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachCompanionInItsOwnersDeadPileAndForNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var deadman = scn.GetFreepsCard("deadman1");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var theirKnight = scn.GetShadowCard("knight");
		// The Dead Man is a companion in play, and no stronger for anyone who dies.
		scn.MoveCompanionsToTable(warrior1, warrior2, deadman);
		// A companion in the discard pile.
		scn.MoveCardsToDiscard(guard);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(warrior1));
		assertEquals(6, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(deadman));

		// A companion in the other player's dead pile.
		scn.MoveCardsToDeadPile(theirKnight);
		assertInZone(Zone.DEAD, theirKnight);
		assertEquals(6, scn.GetStrength(warrior1));

		// An ally in the dead pile.
		scn.MoveCardsToDeadPile(merchant);
		assertInZone(Zone.DEAD, merchant);
		assertEquals(6, scn.GetStrength(warrior1));
		assertEquals(6, scn.GetStrength(warrior2));

		// A [gondor] Man.
		scn.MoveCardsToDeadPile(knight);
		assertEquals(7, scn.GetStrength(warrior1));
		assertEquals(7, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(deadman));

		// A [dwarven] Dwarf.
		scn.MoveCardsToDeadPile(gimli);
		assertEquals(8, scn.GetStrength(warrior1));
		assertEquals(8, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(deadman));

		assertInDiscard(guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneMoreForEachWraithInItsOwnersDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var knight = scn.GetFreepsCard("knight");
		var theirDeadman = scn.GetShadowCard("deadman1");
		// One Wraith in play beside the Warrior, and one in the discard pile.
		scn.MoveCompanionsToTable(warrior1, warrior2);
		scn.MoveCardsToDiscard(deadman2);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(warrior1));

		// A [gondor] companion who is no Wraith counts once, as a companion.
		scn.MoveCardsToDeadPile(knight);
		assertEquals(7, scn.GetStrength(warrior1));

		// A Wraith in the other player's dead pile.
		scn.MoveCardsToDeadPile(theirDeadman);
		assertInZone(Zone.DEAD, theirDeadman);
		assertEquals(7, scn.GetStrength(warrior1));

		// A Wraith who is a companion counts twice: 7 + 1 as a companion + 1 as a Wraith.
		scn.MoveCardsToDeadPile(deadman1);
		assertEquals(9, scn.GetStrength(warrior1));

		// The other Warrior, once dead, is a Wraith and a companion like any other.
		assertEquals(9, scn.GetStrength(warrior2));
		scn.MoveCardsToDeadPile(warrior2);
		assertEquals(11, scn.GetStrength(warrior1));

		assertInDiscard(deadman2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherWarriorKilledInASkirmishMakesItStrongerByTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(warrior1, warrior2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior2, troop);
		scn.FreepsResolveSkirmish(warrior2);

		assertEquals(6, scn.GetStrength(warrior1));
		assertEquals(6, scn.GetStrength(warrior2));
		assertInZone(Zone.FREE_CHARACTERS, warrior2);

		// 13 against 6 is an overwhelm.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, warrior2);
		// 6 + 1 for a companion in the dead pile + 1 for a Wraith in the dead pile
		assertEquals(8, scn.GetStrength(warrior1));
		assertEquals(0, scn.GetWoundsOn(warrior1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
