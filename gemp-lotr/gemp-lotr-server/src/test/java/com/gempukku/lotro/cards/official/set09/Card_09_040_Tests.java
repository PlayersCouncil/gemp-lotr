package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sack", "9_40");

					put("party", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; Damage +1 is its whole text
					put("retainer", "3_57");      // Isengard Retainer: [isengard] Orc, strength 8, vitality 3; a regroup ability used only on purpose
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SackoftheShireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Sack of the Shire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If an [isengard] minion wins a skirmish, until the regroup phase, make that minion strength +1 for each site you control and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sack");

		assertEquals("Sack of the Shire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnIsengardUrukHaiWhoWinsIsStrengthPlusOneForEachSiteYouControlAndFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var party = scn.GetShadowCard("party");
		var retainer = scn.GetShadowCard("retainer");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		// 8 + 2 = 10: he beats the Retainer and loses to the Raiding Party once it is 11.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.StartGame();
		scn.SkipToSite(3);
		// Sites 1 and 2 are behind both markers: the Shadow player takes both.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(party, retainer);
		scn.MoveCardsToHand(sack);
		assertInZone(Zone.HAND, sack);
		scn.SkipToAssignments();
		// The Retainer is a second [isengard] minion, in a skirmish of his own that has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, party}, new PhysicalCardImpl[]{aragorn, retainer});

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sack));

		// 9 against 6: the Raiding Party wins, and wounds twice for its damage +1.
		scn.ShadowPass();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(sack));
		assertEquals(9, scn.GetStrength(party));
		assertFalse(scn.HasKeyword(party, Keyword.FIERCE));

		scn.ShadowPlayCard(sack);

		// 9 + 1 for each of the 2 sites the Shadow player controls.
		assertEquals(11, scn.GetStrength(party));
		assertTrue(scn.HasKeyword(party, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(retainer));
		assertFalse(scn.HasKeyword(retainer, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, sack);

		// 10 against 8: Aragorn beats the Retainer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(retainer));

		// The Raiding Party alone is to be defended against a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(party));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(retainer));
		scn.FreepsAssignToMinions(aragorn, party);

		// The fierce skirmish is the last thing before the regroup phase: both halves hold through it.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(party));
		assertTrue(scn.HasKeyword(party, Keyword.FIERCE));
		// 11 against 10: Aragorn loses, two wounds for the damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(party));
		assertFalse(scn.HasKeyword(party, Keyword.FIERCE));
	}

	@Test
	public void WithTwoIsengardMinionsWinningOneSkirmishItIsOfferedForEachAndASiteTheOtherPlayerControlsIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var party = scn.GetShadowCard("party");
		var retainer = scn.GetShadowCard("retainer");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// 8 + 10 = 18, so that the three minions together (24) beat him without being double him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 is the Free Peoples player's, taken on a turn of his as the Shadow player; site 2 is the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(retainer, party, chieftain);
		scn.MoveCardsToHand(sack);
		assertInZone(Zone.HAND, sack);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, retainer);
		scn.ShadowAssignToMinions(aragorn, party, chieftain);

		// 8 + 9 + 7 against 18: all three win, and the Raiding Party's damage +1 makes it two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(sack));

		// The event is offered once for each [isengard] winner, and not for a [sauron] Orc who won the same skirmish.
		assertTrue(scn.ShadowActionAvailable("Play Sack of the Shire (for Isengard Retainer)"));
		assertTrue(scn.ShadowActionAvailable("Play Sack of the Shire (for Uruk-hai Raiding Party)"));
		assertFalse(scn.ShadowActionAvailable("Orc Chieftain"));
		assertEquals(2, scn.ShadowGetActionCount());

		scn.ShadowChooseAction("Play Sack of the Shire (for Uruk-hai Raiding Party)");

		// 9 + 1: the one site the Shadow player controls, and not the Free Peoples player's.
		assertEquals(10, scn.GetStrength(party));
		assertTrue(scn.HasKeyword(party, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(retainer));
		assertFalse(scn.HasKeyword(retainer, Keyword.FIERCE));
		assertEquals(7, scn.GetStrength(chieftain));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, sack);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnIsengardMinionLosesNorWhenASauronOrcWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var party = scn.GetShadowCard("party");
		var chieftain = scn.GetShadowCard("chieftain");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		// 8 + 2 = 10: he beats the Raiding Party.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.StartGame();
		scn.MoveMinionsToTable(party, chieftain);
		scn.MoveCardsToHand(sack);
		assertInZone(Zone.HAND, sack);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, chieftain}, new PhysicalCardImpl[]{aragorn, party});

		// 7 against 6: an Orc Chieftain, a [sauron] Orc, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 9 against 10: the Raiding Party, an [isengard] minion, loses and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(party));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.HAND, sack);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
