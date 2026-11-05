package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_050_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// is at The Troll Hoard 30_50 (site 2); Goblin Town 30_52 is site 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underling", "33_50");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, site 4; archer only at site 9, a Regroup ability not used
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, site 4; texts on a win and in regroup only
					// Azog, Servant of Sauron: unique [sauron] Orc, twilight 4, site 5; texts on a win and in the maneuver phase only
					put("azog", "32_35");
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, site 5; an Assignment ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger never fires (cheated in)
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, strength 4, vitality 2; a Shadow ability not used

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability not used
					put("kili", "30_12");       // Kili, Brother of Fili: [dwarven] archer companion; Archery: exert Kili to wound a minion
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrcUnderlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Orc Underling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: The site number of each [sauron] Orc is -1.
		 * 	Response: If an Orc is about to take a wound, remove (2) to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("underling");

		assertEquals("Orc Underling", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheSiteNumberOfEachSauronOrcIsMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var veteran = scn.GetShadowCard("veteran");
		var azog = scn.GetShadowCard("azog");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, sauron, runner);
		scn.MoveCardsToHand(azog);
		assertInHand(azog);
		assertEquals(4, scn.GetMinionSiteNumber(veteran));
		assertEquals(5, scn.GetMinionSiteNumber(azog));
		assertEquals(5, scn.GetMinionSiteNumber(sauron));
		assertEquals(4, scn.GetMinionSiteNumber(runner));

		scn.MoveMinionsToTable(underling);
		// The Underling's own and two other [sauron] Orcs', one of them in hand.
		assertEquals(4, scn.GetMinionSiteNumber(underling));
		assertEquals(3, scn.GetMinionSiteNumber(veteran));
		assertEquals(4, scn.GetMinionSiteNumber(azog));
		// A [sauron] minion who is no Orc, and an Orc of another culture.
		assertEquals(5, scn.GetMinionSiteNumber(sauron));
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtSite4ItIsRoamingButASauronOrcPlayedAfterItIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var azog = scn.GetShadowCard("azog");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(underling, azog);
		assertInHand(underling, azog);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Its own text does not name it, and is not on it as it is played (CR 4.2 "modifier"): site 5 at site 4.
		scn.ShadowPlayCard(underling);
		// 10 - (1 + 2 roaming)
		assertEquals(7, scn.GetTwilight());
		// In play, its own site number is 4.
		assertFalse(scn.HasKeyword(underling, Keyword.ROAMING));

		assertEquals(4, scn.GetMinionSiteNumber(azog));
		scn.ShadowPlayCard(azog);
		// 7 - 4: Azog (site 5) is at site 4 and not roaming.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.HasKeyword(azog, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesTwoToPreventTheWoundOfAMoriaOrcThatLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var runner = scn.GetShadowCard("runner");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(underling, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetWoundsOn(runner));

		// 7 against 5: the Runner (vitality 1) loses and is about to take a wound that would kill it.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(underling));
		scn.ShadowChooseOptionalTrigger(underling);
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsTheWoundOfAnOrcChosenAmongTheLosersOfOneSkirmishAndNotOfAMinionWhoIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(underling, aggressor, runner, bats);
		// A strength only to beat three minions at once without doubling them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 9));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.ShadowAssignToMinions(balin, runner, bats);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(2);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(bats));

		// 16 against 6 + 5 + 4: all three lose, and their wounds come together.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(underling));
		scn.ShadowChooseOptionalTrigger(underling);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, underling));
		scn.ShadowChooseCard(aggressor);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(bats));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsItsOwnWoundFromAnArcheryAbilityAndIsNotOfferedWithOneInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var veteran = scn.GetShadowCard("veteran");
		var kili = scn.GetFreepsCard("kili");
		scn.MoveCompanionsToTable(kili);

		scn.StartGame();
		scn.MoveMinionsToTable(underling, veteran);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetWoundsOn(underling));
		assertEquals(0, scn.GetWoundsOn(kili));

		// A [sauron] Orc: the Underling itself.
		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(underling);
		assertEquals(1, scn.GetWoundsOn(kili));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(underling));
		scn.ShadowChooseOptionalTrigger(underling);
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(underling));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// With 1 in the pool the (2) cannot be removed: no offer, and the wound is taken.
		scn.ShadowPass();
		scn.FreepsUseCardAction(kili);
		scn.FreepsChooseCard(underling);
		assertEquals(2, scn.GetWoundsOn(kili));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(underling));
		assertEquals(1, scn.GetWoundsOn(underling));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedForTheWoundOfAMinionWhoIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(underling, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bats);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(2);
		assertEquals(0, scn.GetWoundsOn(bats));

		// 7 against 4: the [moria] Creature loses and takes its wound, with (2) in the pool.
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(underling));
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
