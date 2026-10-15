package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("parry", "1_132");

					put("brawler", "11_179");   // Brawling Uruk: [uruk-hai] Uruk-hai, strength 8, vitality 3; a response to a companion's special ability (none is used)
					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] possession, strength +1 and vitality +1, on an [uruk-hai] Uruk-hai; no other text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1 and nothing else
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7; a regroup ability used only on purpose

					put("sword", "1_299");      // Hobbit Sword: [shire] possession, strength +2, on a Hobbit; no other text
					put("anduril", "7_80");     // Andúril, King's Blade: [gondor] ARTIFACT, strength +2, on Aragorn; defender +1 while no threat
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ParryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Parry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot an Uruk-hai to cancel the strength bonus from a possession until the regroup phase.
		*/

		var scn = GetScenario();

		var parry = scn.GetFreepsCard("parry");

		assertEquals("Parry", parry.getBlueprint().getTitle());
		assertNull(parry.getBlueprint().getSubtitle());
		assertFalse(parry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, parry.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, parry.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, parry.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(parry, Timeword.MANEUVER));
		assertEquals(2, parry.getBlueprint().getTwilightCost());
	}

	@Test
	public void CancelsTheStrengthBonusOfTheChosenPossessionUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parry = scn.GetShadowCard("parry");
		var brawler = scn.GetShadowCard("brawler");
		var spear = scn.GetShadowCard("spear");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(parry);

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(frodo, sword);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(brawler, raiders, servant);
		scn.AttachCardsTo(brawler, spear);
		assertAttachedTo(spear, brawler);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(brawler));      // 8 + 1 for the Spear
		assertEquals(4, scn.GetVitality(brawler));      // 3 + 1 for the Spear
		assertEquals(6, scn.GetStrength(frodo));        // 3 + 1 for The Ruling Ring + 2 for the Hobbit Sword
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2 for Andúril
		assertTrue(scn.ShadowPlayAvailable(parry));

		scn.ShadowPlayCard(parry);
		// A possession of either side, on a minion or on a companion; not an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, spear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(anduril));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(spear);

		assertInDiscard(parry);
		assertEquals(8, scn.GetStrength(brawler));
		// Only the strength bonus is cancelled: the Spear's vitality stays.
		assertEquals(4, scn.GetVitality(brawler));
		assertAttachedTo(spear, brawler);
		// The possession not chosen keeps its bonus.
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, servant});
		scn.ShadowDeclineAssignments();

		// Aragorn, 10, beats the Raiding Party, 9.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(raiders));

		// In the skirmish that follows one already resolved, the bonus is still cancelled.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, servant));
		assertEquals(8, scn.GetStrength(brawler));
		scn.PassSkirmishActions();

		// At the first decision of the regroup phase it is back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(brawler));
		assertEquals(4, scn.GetVitality(brawler));
		assertEquals(6, scn.GetStrength(frodo));
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parry = scn.GetShadowCard("parry");
		var guard = scn.GetShadowCard("guard");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(parry);

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// An [isengard] Orc is no Uruk-hai.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(parry));

		// A [sauron] Uruk-hai is one.
		scn.MoveMinionsToTable(guard);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(parry));

		assertEquals(6, scn.GetStrength(frodo));        // 3 + 1 for The Ruling Ring + 2 for the Hobbit Sword
		scn.ShadowPlayCard(parry);
		assertEquals(4, scn.GetStrength(frodo));
		assertInDiscard(parry);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoPossessionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parry = scn.GetShadowCard("parry");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(parry);

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The only item in play is an artifact.
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2 for Andúril
		assertTrue(scn.ShadowPlayAvailable(parry));

		scn.ShadowPlayCard(parry);
		assertInDiscard(parry);
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
