package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("go", "7_105");
					put("knight", "5_35");          // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("merchant", "7_97");        // Gondorian Merchant: the only [gondor] ally, a Man; his maneuver ability is used only on purpose
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit)
					put("guard", "5_83");           // Household Guard: a [rohan] Man companion, Valiant and no other text

					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] support condition, cost 0, played only to take the hand from four to three
					put("filler1", "1_177");        // Goblin Patrol Troop, three copies as cards held in hand
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");

					// Mordor Fighter: [sauron] Orc; while a threat and another [sauron] Orc can be spotted, the Shadow has
					// initiative whatever the Free Peoples hand holds
					put("fighter1", "7_286");
					put("fighter2", "7_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IWillGoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: I Will Go
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype:
		 * Game Text: Bearer must be a [gondor] Man.<br>If you have initiative, bearer is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var go = scn.GetFreepsCard("go");

		assertEquals("I Will Go", go.getBlueprint().getTitle());
		assertNull(go.getBlueprint().getSubtitle());
		assertTrue(go.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, go.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, go.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, go.getBlueprint().getCardType());
		assertEquals(3, go.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeAGondorManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var go = scn.GetFreepsCard("go");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(go);
		assertInHand(go);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(go));
		scn.FreepsPlayCard(go);
		// A [gondor] Man companion and the [gondor] Man ally; not a [gondor] Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(merchant);
		assertAttachedTo(go, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsDefenderPlusOneWhileTheFreePeoplesPlayerHasInitiativeAndNotOnceHisHandFallsToThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var go = scn.GetFreepsCard("go");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, go);
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertAttachedTo(go, knight);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DEFENDER));
		// Another [gondor] Man who does not bear it is not.
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));

		// Four cards to three: the Shadow has initiative.
		scn.FreepsPlayCard(stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(knight, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotDefenderWhileTheShadowHasInitiativeWhateverTheHandHolds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var go = scn.GetFreepsCard("go");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, go);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"),
				scn.GetFreepsCard("filler4"), scn.GetFreepsCard("stairs"));
		assertAttachedTo(go, knight);
		assertEquals(5, scn.GetFreepsHandCount());

		scn.StartGame();

		// Two Mordor Fighters and a threat: the Shadow has initiative with five cards in the Free Peoples hand.
		scn.MoveMinionsToTable(scn.GetShadowCard("fighter1"), scn.GetShadowCard("fighter2"));
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DEFENDER));

		// The threat gone, the hand of five gives the Free Peoples player initiative again.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
