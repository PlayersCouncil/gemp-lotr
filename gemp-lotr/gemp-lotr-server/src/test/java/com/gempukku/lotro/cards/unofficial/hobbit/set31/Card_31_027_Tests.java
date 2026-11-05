package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_027_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow and
	// regroup phases of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg1", "31_27");
					put("warg2", "31_27");

					put("thorin", "30_19");     // Thorin, Oakenshield: the one card titled Thorin, [dwarven] companion
					put("thorin3", "17_6");     // Thorin III, Stonehelm: a [dwarven] companion of another title; a loss trigger
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal with nothing to heal, a regroup ability not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, vitality 3; a regroup ability not used

					// [gundabad] Orcs, vitality 2.  The Aggressor's assignment ability and Yazneg's maneuver ability are not used.
					put("aggressor", "30_39");  // Orkish Aggressor
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; texts on assigning him, a regroup ability
					put("malevolent", "33_47"); // Malevolent Orc: a [sauron] Orc; an end-of-Shadow-phase offer with nothing to take
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: Bearer must be a [gundabad] Orc.
		 * Bearer is <b>fierce</b>.
		 * <b>Regroup:</b> If you cannot spot Thorin, exert bearer to wound an ally.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("warg1");

		assertEquals("Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAGundabadOrcOnlyAndMakesThatBearerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg1");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var malevolent = scn.GetShadowCard("malevolent");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		assertInZone(Zone.HAND, warg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(aggressor, yazneg, ferny, malevolent);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		scn.ShadowPlayCard(warg);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, malevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(warg, aggressor);
		assertTrue(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ferny, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionWithoutThorinExertsBearerToWoundAnAllyOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg1 = scn.GetShadowCard("warg1");
		var warg2 = scn.GetShadowCard("warg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin3, balin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, bard);
		// Vitality 3 with 2 wounds: Bard is exhausted, so a wound kills him where an exertion could not be made.
		scn.AddWoundsToChar(bard, 2);
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes, each on a Warg; the second copy's bearer is not exerted.
		scn.MoveMinionsToTable(aggressor, yazneg);
		scn.AttachCardsTo(aggressor, warg1);
		scn.AttachCardsTo(yazneg, warg2);
		assertAttachedTo(warg1, aggressor);
		assertAttachedTo(warg2, yazneg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(bard));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// Thorin III is no Thorin: the action is offered beside him.
		assertTrue(scn.ShadowActionAvailable(warg1));
		scn.ShadowUseCardAction(warg1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, bard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, balin, thorin3));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bard);
		assertInZone(Zone.DEAD, bard);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileThorinCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg1 = scn.GetShadowCard("warg1");
		var aggressor = scn.GetShadowCard("aggressor");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg1);
		assertAttachedTo(warg1, aggressor);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warg1));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
	}

	@Test
	public void RegroupActionIsNotOfferedWhenBearerCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg1 = scn.GetShadowCard("warg1");
		var warg2 = scn.GetShadowCard("warg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor, yazneg);
		scn.AttachCardsTo(aggressor, warg1);
		scn.AttachCardsTo(yazneg, warg2);
		// Vitality 2 + 1 with 2 wounds: the Aggressor is exhausted.
		scn.AddWoundsToChar(aggressor, 2);
		assertAttachedTo(warg1, aggressor);
		assertAttachedTo(warg2, yazneg);
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warg1));
		// The other copy, whose bearer can exert, is offered.
		assertTrue(scn.ShadowActionAvailable(warg2));
		assertEquals(2, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
	}

	@Test
	public void WithNoAllyTheCostIsPaidAndNobodyIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg1 = scn.GetShadowCard("warg1");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg1);
		assertAttachedTo(warg1, aggressor);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warg1));
		scn.ShadowUseCardAction(warg1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
