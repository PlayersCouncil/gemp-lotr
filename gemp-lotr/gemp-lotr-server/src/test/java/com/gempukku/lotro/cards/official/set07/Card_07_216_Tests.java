package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_216_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "7_216");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit who is not the Ring-bearer
					put("smeagol", "7_72");     // Sméagol, Hurried Guide: a Ring-bound [gollum] companion who is no Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					// Cards for the Free Peoples hand, which decides who has initiative.
					put("hand1", "1_13");
					put("hand2", "1_13");
					put("hand3", "1_13");
					put("hand4", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Nelya, Black-Mantled Wraith
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>.<br><b>Regroup:</b> If you have initiative, discard Úlairë Nelya to exert each Ring-bound companion.
		*/

		var scn = GetScenario();

		var nelya = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", nelya.getBlueprint().getTitle());
		assertEquals("Black-Mantled Wraith", nelya.getBlueprint().getSubtitle());
		assertTrue(nelya.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nelya.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, nelya.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nelya.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, nelya.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(5, nelya.getBlueprint().getTwilightCost());
		assertEquals(10, nelya.getBlueprint().getStrength());
		assertEquals(3, nelya.getBlueprint().getVitality());
		assertEquals(2, nelya.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeRegroupAbilityDiscardsHerToExertEachRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, smeagol, aragorn);
		// Vitality 4 with 3 wounds: Sam is exhausted and cannot exert. A wound would kill him.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(nelya);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		assertInDiscard(nelya);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		// Exhausted: not exerted, and not wounded either.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		// Unbound.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(nelya);
		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
	}
}
