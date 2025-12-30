package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("iamnoman", "103_84");
					put("eowyn", "5_122");       // Eowyn
					put("haldir", "102_8");      // Haliant companion (valiant Elf)
					put("aragorn", "1_89");      // Non-valiant

					put("witchking", "1_237");
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IAmNoManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: I Am No Man
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a skirmishing valiant companion twice (or spot Eowyn in a skirmish) to make a minion strength -1 for each hindered card you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("iamnoman");

		assertEquals("I Am No Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ======== COST TESTS ========

	@Test
	public void IAmNoManExertsValiantCompanionTwiceAsCost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var haldir = scn.GetFreepsCard("haldir");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(haldir, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(0, scn.GetWoundsOn(haldir));
		int wkStrength = scn.GetStrength(witchking);

		scn.FreepsPlayCard(iamnoman);

		// Haldir exerted twice
		assertEquals(2, scn.GetWoundsOn(haldir));

		// Haldir is now wounded, so minion gets -1
		assertEquals(wkStrength - 1, scn.GetStrength(witchking));
	}

	@Test
	public void IAmNoManIsFreeWithEowynInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(eowyn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(0, scn.GetWoundsOn(eowyn));

		scn.FreepsPlayCard(iamnoman);

		// Eowyn NOT exerted - free cost
		assertEquals(0, scn.GetWoundsOn(eowyn));
	}

	@Test
	public void IAmNoManNotPlayableWithNonValiantCompanionInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn is not valiant, card not playable
		assertFalse(scn.FreepsPlayAvailable(iamnoman));
	}

// ======== STRENGTH CALCULATION TESTS ========

	@Test
	public void IAmNoManCountsHinderedCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking, savage);
		scn.HinderCard(aragorn);
		scn.HinderCard(savage);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(eowyn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		int wkStrength = scn.GetStrength(witchking);

		scn.FreepsPlayCard(iamnoman);

		// 2 hindered cards (Aragorn, Savage)
		assertEquals(wkStrength - 2, scn.GetStrength(witchking));
	}

	@Test
	public void IAmNoManCountsWoundedCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking, savage);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(savage, 2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(eowyn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		int wkStrength = scn.GetStrength(witchking);

		scn.FreepsPlayCard(iamnoman);

		// 2 wounded cards (Aragorn with 1, Savage with 2) - counts cards, not wounds
		assertEquals(wkStrength - 2, scn.GetStrength(witchking));
	}

	@Test
	public void IAmNoManHinderedAndWoundedCardCountsOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking);

		// Aragorn is both wounded AND hindered
		scn.AddWoundsToChar(aragorn, 1);
		scn.HinderCard(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(eowyn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		int wkStrength = scn.GetStrength(witchking);

		scn.FreepsPlayCard(iamnoman);

		// Aragorn counts as 1 (hindered), not 2 (hindered + wounded)
		// Hindered cards shouldn't be spottable as wounded
		assertEquals(wkStrength - 1, scn.GetStrength(witchking));
	}

	@Test
	public void IAmNoManCombinesHinderedAndWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		var witchking = scn.GetShadowCard("witchking");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(eowyn, aragorn, haldir);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(witchking, savage);

		// Aragorn is hindered (1)
		scn.HinderCard(aragorn);
		// Haldir is wounded (1)
		scn.AddWoundsToChar(haldir, 1);
		// Savage is wounded (1)
		scn.AddWoundsToChar(savage, 1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(eowyn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);

		int wkStrength = scn.GetStrength(witchking);

		scn.FreepsPlayCard(iamnoman);

		// 1 hindered + 2 wounded = -3
		assertEquals(wkStrength - 3, scn.GetStrength(witchking));
	}
}
