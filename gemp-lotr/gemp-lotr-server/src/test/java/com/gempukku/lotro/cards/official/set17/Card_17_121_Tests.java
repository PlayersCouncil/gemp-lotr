package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "17_121");
					// The Ring-bearer, Frodo 1_290, is the [shire] culture.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a second [gondor] companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion; a skirmish ability used only on purpose
					put("cart", "1_73");        // Gandalf's Cart: a [gandalf] possession in the support area, no character; a fellowship ability only
					put("gollum", "5_25");      // Gollum, Stinker: a [gollum] Shadow card, which shows no Free Peoples culture
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Berserker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>At the start of the archery phase, if you control a site, you may add 2 to the minion archery total for each Free Peoples culture you can spot over 2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("berserker");

		assertEquals("White Hand Berserker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteControlledAndThreeCulturesItAddsTwoToTheMinionArcheryTotalForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var berserker = scn.GetShadowCard("berserker");

		// Both markers are on site 3 on the first player's third turn, so site 1 can be controlled.
		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(berserker);
		scn.SkipToPhase(Phase.ARCHERY);

		// [shire], [gondor] and [dwarven]: one culture over 2. No archer is in play.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(berserker));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Two archery wounds for the fellowship.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The archery phase is over, and the +2 with it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AfterAMoveInTheRegroupPhaseItIsOfferedAgainAtTheSecondArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var berserker = scn.GetShadowCard("berserker");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(berserker);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(berserker));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);

		// Gimli loses to the Berserker and lives; the fellowship moves again, and the Berserker stays in play.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, berserker);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(berserker));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ACultureShownByAPossessionCountsAndAGollumCardOrASecondGondorCompanionCountsNone()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli");
		scn.MoveCardsToSupportArea(cart);
		assertInZone(Zone.SUPPORT, cart);

		var berserker = scn.GetShadowCard("berserker");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(berserker, gollum);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(berserker));
		scn.ShadowAcceptOptionalTrigger();
		// [shire], [gondor], [dwarven] and the Cart's [gandalf]: two over 2, so 2 x 2. Boromir's [gondor] is
		// counted once with Aragorn's, and Gollum's [gollum] is no Free Peoples culture: either counted would read 6.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndDecliningAtTheArcheryPhaseLeavesTheTotalAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "gimli");

		var berserker = scn.GetShadowCard("berserker");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(berserker);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(berserker));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(berserker));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyTheFreePeoplesPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "gimli");

		var berserker = scn.GetShadowCard("berserker");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 is the Free Peoples player's, as if taken on one of the other player's turns.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(berserker);
		scn.SkipToPhase(Phase.ARCHERY);

		// Three cultures, as in the test above where it is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(berserker));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithTwoCulturesItWouldAddNothingAndIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn", "boromir");

		var berserker = scn.GetShadowCard("berserker");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(berserker);
		scn.SkipToPhase(Phase.ARCHERY);

		// [shire] and [gondor] only, the second [gondor] companion counting for nothing: none over 2.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(berserker));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
