package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_063_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "32_63");

					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, another minion read beside her; its text needs 2 wounds on the Ring-bearer

					// Companions bearing a follower, and not
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion; his texts reach no test here
					put("thrush", "31_42");     // Old Thrush, Messenger Bird: [shire] follower, "Aid - Exert Bilbo"; a maneuver ability not used
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower; his trigger wants a fierce skirmish won
					put("acorn", "31_38");      // An Acorn from Beorn's house: [shire] possession on Bilbo; a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Nelya, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * While you can spot a companion bearing a follower, Úlairë Nelya is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AFollowerTransferredToACompanionMakesNelyaDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var thrush = scn.GetFreepsCard("thrush");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToSupportArea(thrush);
		scn.MoveMinionsToTable(nelya, hound);
		assertInZone(Zone.SUPPORT, thrush);
		scn.SkipToPhase(Phase.MANEUVER);

		// Old Thrush lies in the support area, borne by nobody.
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrush, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		// Bilbo, the one companion, pays the Aid and bears him.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertAttachedTo(thrush, bilbo);

		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		// Nelya alone: the other minion gains nothing.
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void APossessionOnACompanionOrAnUnborneFollowerGivesNothingButAFollowerOnAnotherCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var thrush = scn.GetFreepsCard("thrush");
		var bifur = scn.GetFreepsCard("bifur");
		var acorn = scn.GetFreepsCard("acorn");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(thrush);
		scn.MoveMinionsToTable(nelya, hound);
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, thrush);

		// Bilbo bears a possession, and Old Thrush is in the support area.
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));

		scn.AttachCardsTo(thorin, bifur);
		assertAttachedTo(bifur, thorin);
		// A [dwarven] follower on a Dwarf companion.
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
