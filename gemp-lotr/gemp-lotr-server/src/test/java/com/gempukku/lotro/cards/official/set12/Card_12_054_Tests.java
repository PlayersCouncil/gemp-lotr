package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "12_54");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCultureScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "12_54");
					// Sam, Son of Hamfast: a [shire] companion beside the Ring-bearer, strength 3; his fellowship
					// ability is not used
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit who is an ally, strength 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGondorScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "12_54");
					put("staff", "4_174");      // Saruman's Staff, Wizard's Device: plays on Saruman; fierce, damage +1, and a Maneuver switch
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site 5; damage +1 is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, strength 3 (enduring, unwounded); a skirmish ability used only on purpose
					put("kotd", "8_38");        // King of the Dead, Oathbreaker: [gondor] Wraith companion, strength 7; his Response answers a wound on Aragorn only
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ALLY, strength 6; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion, strength 6; an archery ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Saruman, Of Many Colours
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>When you play Saruman, name a culture. Each companion of the named culture is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Of Many Colours", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NamingACultureMakesEachCompanionOfThatCultureStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCultureScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 3, and 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));

		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.ShadowDecisionAvailable("Choose a culture"));
		assertEquals(4, scn.GetStrength(frodo));

		// Each [shire] companion is one less; the [gondor] companion and the [shire] ally are as they were.
		scn.ShadowChooseOption("Shire");
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NamingGondorReachesEveryGondorCompanionAndAMinionPlayedAfterHimNamesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGondorScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var kotd = scn.GetFreepsCard("kotd");
		var merchant = scn.GetFreepsCard("merchant");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, pippin, kotd, legolas);
		scn.MoveAlliesToTable(merchant);

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(saruman, savage);

		scn.StartGame();
		assertInZone(Zone.HAND, saruman, savage);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(kotd));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(6, scn.GetStrength(legolas));

		scn.ShadowPlayCard(saruman);
		assertTrue(scn.ShadowDecisionAvailable("Choose a culture"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseOption("Gondor");

		// Each [gondor] companion, of whatever race; not the [gondor] ally or the [elven] companion.
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(kotd));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [isengard] minion played after him: no culture is asked, and nothing changes.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowDecisionAvailable("Choose a culture"));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SarumansStaffHasNoEffectOnHimAndItsManeuverAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGondorScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		assertEquals(saruman, staff.getAttachedTo());
		assertInZone(Zone.ATTACHED, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Legacy Ruling 6: the Staff's ability "does not have any effect when used on Saruman, Of Many Colours".
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));
	}
}
