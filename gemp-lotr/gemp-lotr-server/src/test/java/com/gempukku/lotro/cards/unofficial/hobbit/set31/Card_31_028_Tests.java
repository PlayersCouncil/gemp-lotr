package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_028_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow and
	// regroup phases of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wwarg1", "31_28");
					put("wwarg2", "31_28");

					put("thorin", "30_19");     // Thorin, Oakenshield: the one card titled Thorin, [dwarven] companion
					put("thorin3", "17_6");     // Thorin III, Stonehelm: a [dwarven] companion of another title; a loss trigger
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a fellowship ability not used
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal with nothing to heal, a regroup ability not used

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
	public void WarWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: War-Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Vitality: 2
		 * Game Text: Bearer must be a [gundabad] Orc.
		 * Bearer is <b>fierce</b>.
		 * <b>Regroup:</b> If you cannot spot Thorin, exert bearer to make the Free Peoples player wound a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("wwarg1");

		assertEquals("War-Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAGundabadOrcOnlyAndMakesThatBearerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wwarg = scn.GetShadowCard("wwarg1");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var malevolent = scn.GetShadowCard("malevolent");
		scn.MoveCardsToHand(wwarg);

		scn.StartGame();
		assertInZone(Zone.HAND, wwarg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(aggressor, yazneg, ferny, malevolent);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		scn.ShadowPlayCard(wwarg);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, malevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(wwarg, aggressor);
		assertTrue(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ferny, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionWithoutThorinExertsBearerToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wwarg1 = scn.GetShadowCard("wwarg1");
		var wwarg2 = scn.GetShadowCard("wwarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var balin = scn.GetFreepsCard("balin");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin3, balin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 3 with 2 wounds: Balin is exhausted, so a wound kills him where an exertion could not be made.
		scn.AddWoundsToChar(balin, 2);
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes, each on a War-Warg; the second copy's bearer is not exerted.
		scn.MoveMinionsToTable(aggressor, yazneg);
		scn.AttachCardsTo(aggressor, wwarg1);
		scn.AttachCardsTo(yazneg, wwarg2);
		assertAttachedTo(wwarg1, aggressor);
		assertAttachedTo(wwarg2, yazneg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// Thorin III is no Thorin: the action is offered beside him.
		assertTrue(scn.ShadowActionAvailable(wwarg1));
		scn.ShadowUseCardAction(wwarg1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(yazneg));
		// The Free Peoples player chooses the companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin, thorin3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin3));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileThorinCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wwarg1 = scn.GetShadowCard("wwarg1");
		var aggressor = scn.GetShadowCard("aggressor");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, wwarg1);
		assertAttachedTo(wwarg1, aggressor);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wwarg1));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
	}

	@Test
	public void RegroupActionIsNotOfferedWhenBearerCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wwarg1 = scn.GetShadowCard("wwarg1");
		var wwarg2 = scn.GetShadowCard("wwarg2");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor, yazneg);
		scn.AttachCardsTo(aggressor, wwarg1);
		scn.AttachCardsTo(yazneg, wwarg2);
		// Vitality 2 + 2 with 3 wounds: the Aggressor is exhausted.
		scn.AddWoundsToChar(aggressor, 3);
		assertAttachedTo(wwarg1, aggressor);
		assertAttachedTo(wwarg2, yazneg);
		assertEquals(3, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wwarg1));
		// The other copy, whose bearer can exert, is offered.
		assertTrue(scn.ShadowActionAvailable(wwarg2));
		assertEquals(3, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(elrond));
	}
}
