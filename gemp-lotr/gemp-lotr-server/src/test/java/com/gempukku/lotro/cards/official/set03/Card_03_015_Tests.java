package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forests", "3_15");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] ally, home 6; an archery ability used only on purpose
					put("rumil", "1_57");       // Rumil, Elven Protector: [elven] ally, home 6; an archery ability used only on purpose
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally, home 3; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, no ally; +3 only against a Nazgul
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability only

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so exhausted: a wound kills him
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 3 is Rivendell Waterfall: "While you can spot a ranger at Rivendell Waterfall, the move limit is +1
	// for this turn."  Arwen is a ranger, so a fellowship that reaches it on a regroup move may move once more.
	protected VirtualTableScenario GetThreeMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forests", "3_15");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] ally, home 6
					put("rumil", "1_57");       // Rumil, Elven Protector: [elven] ally, home 6
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally, home 3
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion and a ranger

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForestsofLothlorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Forests of Lothlórien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time the fellowship moves, you may spot 3 [elven] allies to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forests");

		assertEquals("Forests of Lothlórien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveWithThreeElvenAlliesWoundsAMinionOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(forests);
		// Exactly three [elven] allies, of both home sites.
		scn.MoveAlliesToTable(orophin, rumil, golradir);
		scn.MoveCompanionsToTable(arwen);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToMovementDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forests));
		scn.FreepsAcceptOptionalTrigger();

		// A minion of either culture and race, the exhausted one among them; no companion and no ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, arwen, orophin, rumil, golradir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		// The Runner had one vitality: the wound kills him.  The Savage is untouched.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveCardsToSupportArea(forests);
		scn.MoveAlliesToTable(orophin, rumil, golradir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();

		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forests));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoElvenAlliesBesideAnElvenCompanionAndADwarvenAllyAreNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var orophin = scn.GetFreepsCard("orophin");
		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToSupportArea(forests);
		// Two [elven] allies; an [elven] card that is no ally and an ally who is not [elven] nearly make three.
		scn.MoveAlliesToTable(orophin, golradir, grimir);
		scn.MoveCompanionsToTable(arwen);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();

		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forests));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveCardsToSupportArea(forests);
		scn.MoveAlliesToTable(orophin, rumil, golradir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forests));

		assertEquals(0, scn.GetWoundsOn(savage));

		// The fellowship stays where it is: the turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forests));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithNoMinionInPlayTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveCardsToSupportArea(forests);
		scn.MoveAlliesToTable(orophin, rumil, golradir);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The move that ends the fellowship phase: three [elven] allies to spot, and no minion to wound.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forests));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveOfATurnOffersTheWoundAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeMoveScenario();

		var forests = scn.GetFreepsCard("forests");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var golradir = scn.GetFreepsCard("golradir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToSupportArea(forests);
		scn.MoveAlliesToTable(orophin, rumil, golradir);
		scn.MoveCompanionsToTable(arwen);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();

		assertEquals(0, scn.GetWoundsOn(savage));

		// The move from site 2 to Rivendell Waterfall.  The Savage is the only minion: chosen unasked.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forests));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With a ranger at the Waterfall the fellowship may move a third time, and the Savage is still there.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forests));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(savage));

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
