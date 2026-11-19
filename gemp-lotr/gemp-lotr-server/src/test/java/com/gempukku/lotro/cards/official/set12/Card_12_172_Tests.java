package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "12_172");
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, strength 10, vitality 3; fierce and damage +1
					// only at 2 burdens or 2 wounds on the Ring-bearer, and no test reaches either
					put("nelya", "2_84");
					// Ulaire Attea, Khamûl of the East: a [raider] Nazgûl, printed fierce, strength 12, vitality 3; the rest
					// of his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl; strength 5, nothing below 2 wounds on the
					// Ring-bearer
					put("hound", "7_192");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3; abilities used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SteedofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Steed of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Game Text: Bearer must be a Nazgûl.<br>Bearer gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>Each time bearer wins a skirmish, you may draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("steed");

		assertEquals("Steed of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnANazgulOfEitherCultureAndNotOnAWraithOrcAndGivesItsBearerAloneMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(steed);

		scn.StartGame();
		assertInHand(steed);
		scn.MoveMinionsToTable(nelya, khamul, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInHand(steed);
		assertFalse(scn.HasKeyword(nelya, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(khamul, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(hound, Keyword.MUSTER));

		scn.ShadowPlayCard(steed);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));

		scn.ShadowChooseCard(khamul);
		assertAttachedTo(steed, khamul);
		assertTrue(scn.HasKeyword(khamul, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(nelya, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(hound, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachSkirmishItsBearerWinsOffersTwoCardsWhichCanBeDrawnOrDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.AttachCardsTo(khamul, steed);
		assertAttachedTo(steed, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 12 + 1 against 8: Aragorn loses and lives, with 1 wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(steed));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(deck > 2);
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());

		// Khamûl is fierce: a second win in the fierce round, a second offer, declined.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, khamul);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(steed));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());

		// The regroup phase opens on the Shadow player's muster, which his bearer has from the Steed.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Use Muster"));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void NoDrawIsOfferedWhenItsBearerLosesAndLivesOrAnotherNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetShadowCard("steed");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, nelya);
		scn.AttachCardsTo(khamul, steed);
		assertAttachedTo(steed, khamul);
		// Aragorn at 18 beats Khamûl's 13 without doubling it, so Khamûl loses and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul}, new PhysicalCardImpl[]{gimli, nelya});

		// Nelya, a Nazgûl who does not bear the Steed, beats Gimli, 10 against 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(steed));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The bearer loses, 13 against 18, and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(steed));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
