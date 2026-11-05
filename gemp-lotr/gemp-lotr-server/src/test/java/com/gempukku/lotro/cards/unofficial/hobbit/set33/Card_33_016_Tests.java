package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_016_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "33_16");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability, not used
					put("balin", "30_2");
					// Elrond, Elven Lord: [elven] Elf ally, Wise, 8/4; a start-of-turn heal (nothing is wounded at the
					// start of the turn) and a regroup ability, not used
					put("elrond", "30_21");
					// Lore of Imladris: [dwarven] maneuver event, "Heal a companion (or 2 Dwarf companions if you
					// spot Elrond)"; played where no Elrond is in play
					put("lore", "30_13");
					put("lore2", "30_13");
					// Shoulder to Shoulder: [elven] support-area condition, "Maneuver: Add (1) and exert a Dwarf to
					// heal an Elf, or ...": the heal of an ally
					put("shoulder", "1_59");

					// Orkish Aggressor: [gundabad] Orc, 6/2; an assignment ability, not used
					put("aggressor", "30_39");
					// Orkish Veteran: [sauron] Orc, 7/2; a regroup ability, not reached
					put("veteran", "32_43");
					// Bill Ferny Sr.: [gundabad] Man minion, 4/2, no Orc; assignment bars and a regroup ability, not used
					put("ferny", "33_22");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RivendellScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Rivendell Scout
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Site Number: 3H
		 * Game Text: <b>Archer</b>.<br>Each time a companion heals, you may exert this ally to wound an Orc.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Rivendell Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 3)));
	}

	@Test
	public void EachTimeACompanionHealsScoutMayExertToWoundAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var balin = scn.GetFreepsCard("balin");
		var lore = scn.GetFreepsCard("lore");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(scout);

		scn.StartGame();
		scn.MoveCardsToHand(lore);
		scn.AddWoundsToChar(balin, 1);
		scn.MoveMinionsToTable(aggressor, veteran, ferny);
		scn.AddWoundsToChar(aggressor, 1);
		assertInHand(lore);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(aggressor));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);
		// Balin is the one wounded companion: healed unasked.
		assertEquals(0, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scout));
		assertEquals(0, scn.GetWoundsOn(scout));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(scout));

		// Orcs of two cultures are offered; the Man minion is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, veteran));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ferny));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(veteran));
		// The Aggressor has 1 vitality left: the wound kills him.
		scn.FreepsChooseCard(aggressor);
		assertInDiscard(aggressor);
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningScoutsOfferLeavesEveryoneAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var scout = scn.GetFreepsCard("scout");
		var lore = scn.GetFreepsCard("lore");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(scout);

		scn.StartGame();
		scn.MoveCardsToHand(lore);
		scn.AddWoundsToChar(bilbo, 1);
		scn.MoveMinionsToTable(aggressor, veteran);
		assertInHand(lore);
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scout));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnAllyHealingDoesNotOfferScoutsAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var balin = scn.GetFreepsCard("balin");
		var elrond = scn.GetFreepsCard("elrond");
		var shoulder = scn.GetFreepsCard("shoulder");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(scout, elrond);
		scn.MoveCardsToSupportArea(shoulder);

		scn.StartGame();
		scn.AddWoundsToChar(elrond, 1);
		scn.MoveMinionsToTable(aggressor);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(shoulder);
		scn.FreepsChooseCard(balin);
		// Balin exerts; Elrond, the one wounded Elf, is healed unasked.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(elrond));

		// An ally healed is no companion healed: the scout, able to exert, with an Orc in play, is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scout));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOrcInPlayScoutIsOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var scout = scn.GetFreepsCard("scout");
		var lore = scn.GetFreepsCard("lore");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(scout);

		scn.StartGame();
		scn.MoveCardsToHand(lore);
		scn.AddWoundsToChar(balin, 1);
		scn.MoveMinionsToTable(ferny);
		assertInHand(lore);
		assertEquals(1, scn.GetWoundsOn(balin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);
		assertEquals(0, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scout));
		scn.FreepsAcceptOptionalTrigger();
		// The exertion is paid; the one minion is no Orc and is not wounded.
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedScoutIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var scout = scn.GetFreepsCard("scout");
		var lore = scn.GetFreepsCard("lore");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(scout);

		scn.StartGame();
		scn.MoveCardsToHand(lore);
		scn.AddWoundsToChar(bilbo, 1);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(scout, 2);
		scn.MoveMinionsToTable(aggressor);
		assertInHand(lore);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(scout));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scout));
		assertEquals(2, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachHealOfTheSameCompanionOffersScoutsAbilityAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var scout = scn.GetFreepsCard("scout");
		var lore = scn.GetFreepsCard("lore");
		var lore2 = scn.GetFreepsCard("lore2");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(scout);

		scn.StartGame();
		scn.MoveCardsToHand(lore, lore2);
		scn.AddWoundsToChar(bilbo, 2);
		scn.MoveMinionsToTable(aggressor, veteran);
		assertInHand(lore, lore2);
		assertEquals(2, scn.GetWoundsOn(bilbo));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scout));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aggressor);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(veteran));

		scn.ShadowPass();
		// Bilbo heals a second time: the scout is offered again.
		scn.FreepsPlayCard(lore2);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scout));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(veteran);
		assertEquals(2, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
