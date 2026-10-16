package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_152_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shaman", "1_152");
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai minion, vitality 3; Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai minion, vitality 2; Damage +1 is its whole text
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc minion, vitality 3; a regroup ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukShamanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Shaman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Remove (2) to heal an Uruk-hai.
		*/

		var scn = GetScenario();

		var shaman = scn.GetFreepsCard("shaman");

		assertEquals("Uruk Shaman", shaman.getBlueprint().getTitle());
		assertNull(shaman.getBlueprint().getSubtitle());
		assertFalse(shaman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shaman.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, shaman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, shaman.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, shaman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(shaman, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(shaman, Keyword.DAMAGE));
		assertEquals(3, shaman.getBlueprint().getTwilightCost());
		assertEquals(8, shaman.getBlueprint().getStrength());
		assertEquals(2, shaman.getBlueprint().getVitality());
		assertEquals(5, shaman.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverRemovesTwoTwilightToHealOneWoundOfAnUrukHaiOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var party = scn.GetShadowCard("party");
		var sentry = scn.GetShadowCard("sentry");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, party, sentry, servant);
		scn.AddWoundsToChar(shaman, 1);
		// Two wounds on the Raiding Party, so that healing one wound is told from healing two.
		scn.AddWoundsToChar(party, 2);
		scn.AddWoundsToChar(sentry, 1);
		scn.AddWoundsToChar(servant, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(servant));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(0, scn.GetTwilight());

		// The Shaman himself and Uruk-hai of two cultures; not the [isengard] Orc, nor a wounded companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shaman, party, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(party);
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoUrukHaiWoundedTheTwoTwilightAreRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, servant);
		scn.AddWoundsToChar(servant, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(servant));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		// The ability has a cost, so it is offered with only an Orc and a companion wounded; nothing is healed.
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, party);
		scn.AddWoundsToChar(party, 1);
		assertEquals(1, scn.GetWoundsOn(party));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void NotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, party);
		scn.AddWoundsToChar(party, 1);
		assertEquals(1, scn.GetWoundsOn(party));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(party));
	}
}
