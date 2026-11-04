package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_184_Tests
{

	// The Towers path with Uruk Camp as site 2: Battleground, Plains. Its "each time an Uruk-hai is played, that minion
	// must exert" answers no minion here, since each is cheated onto the table.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("force", "11_184");
					put("tyrannical", "11_204");    // Tyrannical Uruk: [uruk-hai] Uruk-hai minion, Damage +1, Toil 1, no other text
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai minion, Damage +1, no other text
					put("worthy", "12_160");        // Worthy of Mordor: [uruk-hai] support-area condition; acts only at the start of the regroup phase
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] Man to exert for Defend It and Hope
					put("defend1", "4_115");        // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once
					put("defend2", "4_115");        //   (or twice if that minion is an Uruk-hai)
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");          // Uruk Camp: Battleground. Plains.
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The plain Towers path: site 2 is Eastfold, a plains and no battleground.
	protected VirtualTableScenario GetEastfoldScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("force", "11_184");
					put("tyrannical", "11_204");
					put("boromir", "1_97");
					put("defend1", "4_115");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForceofUrukhaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Force of Uruk-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion is about to take a wound at a battleground site, you may spot another [uruk-hai] card to make the Free Peoples player draw a card instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("force");

		assertEquals("Force of Uruk-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachWoundAboutToBeTakenAtABattlegroundMayBeTurnedIntoADrawForTheFreePeoplesPlayerWhileAnotherUrukhaiMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var force = scn.GetShadowCard("force");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1, defend2);

		scn.StartGame();
		scn.MoveMinionsToTable(force, tyrannical);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(0, scn.GetWoundsOn(tyrannical));
		assertEquals(2, scn.GetFreepsHandCount());
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(defend1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(force));
		assertTrue(scn.FreepsHasCardChoiceAvailable(tyrannical));
		scn.FreepsChooseCard(force);

		// The first of the two wounds: offered, and taken.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(force));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(2, scn.GetFreepsHandCount());  // one Defend It and Hope played, one card drawn
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// The second wound is offered again, and declined: the wound is taken and nothing is drawn.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(force));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(force));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Another Uruk-hai minion's wounds at the same battleground are not answered: "this minion".
		scn.ShadowPass();
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(tyrannical);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(tyrannical));
		assertEquals(1, scn.GetWoundsOn(force));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUrukhaiConditionIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var force = scn.GetShadowCard("force");
		var worthy = scn.GetShadowCard("worthy");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1);
		scn.MoveCardsToSupportArea(worthy);

		scn.StartGame();
		scn.MoveMinionsToTable(force);
		scn.SkipToPhase(Phase.MANEUVER);

		// The minion is the only [uruk-hai] minion; the condition is the other [uruk-hai] card.
		assertInZone(Zone.SUPPORT, worthy);
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(1, scn.GetFreepsHandCount());

		// The only minion is chosen unasked.
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(force));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(force));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyAnIsengardUrukhaiBesideItTheWoundsAreTakenAndNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var force = scn.GetShadowCard("force");
		var savage = scn.GetShadowCard("savage");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1);

		scn.StartGame();
		scn.MoveMinionsToTable(force, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// No other [uruk-hai] card: the Savage is an Uruk-hai of [isengard], and the minion does not spot itself.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(force);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(force));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtASiteThatIsNoBattlegroundTheWoundsAreTakenAndNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEastfoldScenario();

		var force = scn.GetShadowCard("force");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1);

		scn.StartGame();
		scn.MoveMinionsToTable(force, tyrannical);
		scn.SkipToPhase(Phase.MANEUVER);

		// Eastfold: a plains, no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(force);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(force));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyNothingIsOfferedAndTheWoundsAreTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var force = scn.GetShadowCard("force");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1);

		scn.StartGame();
		scn.MoveMinionsToTable(force, tyrannical);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck();

		// CR 4.2 "instead": "If the second effect cannot happen for any reason, then the original effect occurs."
		// With nothing to draw the trigger would do nothing (Q-0034), and the wounds are taken.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(force));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(force);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(force));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
