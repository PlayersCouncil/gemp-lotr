package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "15_19");

					put("uruk1", "15_171");     // Tracking Uruk: [uruk-hai] Uruk-hai minion, Hunter 1 and nothing else
					put("uruk2", "15_171");
					put("headsman", "18_89");   // Orkish Headsman: [orc] Orc minion, Hunter 1; +3 strength for each companion in the dead pile
					put("herdsman", "15_83");   // Hunting Herdsman: [men] Man minion, Hunter 1; +3 strength for each follower, and none is
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion who is no hunter; a "when you play" trigger, cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Legolas, Of the Woodland Realm
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Archer</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br><b>Archery:</b> Exert Legolas to make the fellowship archery total +1 for each hunter minion you can spot (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Of the Woodland Realm", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void ArcheryAbilityExertsLegolasToMakeTheFellowshipArcheryTotalPlusOneForOneHunterMinionForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var uruk1 = scn.GetShadowCard("uruk1");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Legolas is an archer himself; he is a hunter too, but no minion.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// In force as the fire is placed: two wounds on the only minion, not one. Gone with the phase: the
		// assignment phase reads Legolas's own 1 again.
		assertEquals(0, scn.GetWoundsOn(uruk1));
		scn.PassArcheryActions();
		assertEquals(2, scn.GetWoundsOn(uruk1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void TwoHunterMinionsOfOtherCulturesAndRacesMakeItPlusTwoAndAMinionWhoIsNoHunterIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var headsman = scn.GetShadowCard("headsman");
		var herdsman = scn.GetShadowCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(headsman, herdsman, runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		// 1 for Legolas + 1 for the Headsman + 1 for the Herdsman; the Goblin Runner adds nothing.
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNoHunterMinionTheAbilityIsOfferedAndTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithFourHunterMinionsItAddsNoMoreThanThreeInThePhaseAndASecondUseIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var headsman = scn.GetShadowCard("headsman");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2, headsman, herdsman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsUseCardAction(legolas);
		// Four hunter minions, held to +3.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(4, scn.GetFreepsArcheryTotal());

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// At the limit the ability is still offered, its exertion payable, and adds nothing.
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(4, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void TheLimitIsTheArcheryPhasesAndANewArcheryPhaseGivesPlusThreeAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var headsman = scn.GetShadowCard("headsman");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2, headsman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(4, scn.GetFreepsArcheryTotal());

		// The minions are taken off before the fire, and the turns played out to player 1's next turn.
		scn.MoveCardsToDiscard(uruk1, uruk2, headsman);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		// Player 1's fellowship phase again, at site 2, where his fellowship stopped.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(uruk1, uruk2, headsman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(4, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileLegolasIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var uruk1 = scn.GetShadowCard("uruk1");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// Vitality 3 with 2 wounds.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.MoveMinionsToTable(uruk1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
	}
}
