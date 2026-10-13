package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gats", "15_86");       // Mûmak Commander, Giant Among the Swertings
					// Pavise: [men] possession, "Bearer must be a [men] minion", vitality +2; it stops wounds outside a
					// skirmish, not exertions.  With it the Commander can pay for his ability twice.
					put("pavise", "11_94");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; Ring-bound, and not the Ring-bearer
					put("gimli", "1_13");       // Gimli, Son of Glóin: vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally, vitality 3; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mûmak Commander, Giant Among the Swertings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Exert Mûmak Commander twice to exert a companion twice (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gats");

		assertEquals("Mûmak Commander", card.getBlueprint().getTitle());
		assertEquals("Giant Among the Swertings", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverActionExertsTheCommanderTwiceToExertAChosenCompanionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gats = scn.GetShadowCard("gats");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		// Gimli has 2 vitality left: he could exert once, not twice.
		scn.AddWoundsToChar(gimli, 1);
		scn.MoveMinionsToTable(gats);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gats));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.ShadowActionAvailable(gats));

		scn.ShadowUseCardAction(gats);
		assertEquals(2, scn.GetWoundsOn(gats));
		// A companion of another culture and a Ring-bound companion who is not the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		// Not the Ring-bearer, not a companion who cannot exert twice, and not an ally: Frodo and Grimir are
		// unwounded and could each exert twice.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, gimli, grimir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(aragorn);
		// Vitality 4: two exertions, not one and not exhausted.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(2, scn.GetWoundsOn(gats));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheCommanderCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gats = scn.GetShadowCard("gats");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(gats);
		scn.AddWoundsToChar(gats, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with one wound: he can exert once, and the cost is two exertions.  Aragorn could exert twice.
		assertEquals(2, scn.GetVitality(gats));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowActionAvailable(gats));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertTwiceACompanionWhoCanExertOnceIsExertedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gats = scn.GetShadowCard("gats");
		var pavise = scn.GetShadowCard("pavise");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(gats);
		// Vitality 3 + 2: four exertions can be paid.
		scn.AttachCardsTo(gats, pavise);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gats));
		assertEquals(0, scn.GetWoundsOn(sam));

		// Sam is the only companion who is not the Ring-bearer: nothing is asked.
		scn.ShadowUseCardAction(gats);
		assertEquals(2, scn.GetWoundsOn(gats));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		// Sam has 2 vitality left and can exert only once more.  With nobody who could exert twice, the ability
		// is still his to be exerted by, as far as he can go.
		assertTrue(scn.ShadowActionAvailable(gats));

		scn.ShadowUseCardAction(gats);
		assertEquals(4, scn.GetWoundsOn(gats));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetVitality(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
