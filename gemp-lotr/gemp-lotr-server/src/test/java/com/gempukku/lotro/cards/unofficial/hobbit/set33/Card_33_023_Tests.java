package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_023_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Shadow phase of
	// turn 1 is at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "33_23");
					put("warg2", "33_23");

					// Yazneg, Orkish Assassin: the one Yazneg; [gundabad] Orc, strength 5, vitality 2; a maneuver
					// ability only
					put("yazneg", "30_41");
					// Goblin Footman: [moria] Orc, strength 8, vitality 1; a play trigger only
					put("footman", "30_42");
					// Orkish Veteran: [sauron] Orc, strength 7, vitality 2; a regroup ability used only on purpose,
					// and archer only at site 9
					put("veteran", "32_43");
					// Dark Bats: [moria] Creature, a minion who is no Orc; a Shadow ability only
					put("bats", "32_30");
					// Goblin Scimitar: [moria] hand weapon possession on an Orc, a Shadow possession; a play trigger only
					put("scimitar", "31_31");

					// Dwalin, Brother of Balin: [dwarven] Dwarf companion; a maneuver ability only
					put("dwalin", "30_9");
					// Dwarven Axe: [dwarven] hand weapon possession, borne by Dwalin; damage +1 at a mountain
					put("axe", "31_1");
					// Old Toby: [shire] pipeweed possession in the support area; an optional draw on its own play only
					put("toby", "1_305");
					// Thorin, Oakenshield, and Orcrist: a Free Peoples artifact on a companion; a trigger on his wins
					put("thorin", "30_19");
					put("orcrist", "30_17");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ScoutingWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Scouting Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 4
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Vitality: 2
		 * Game Text: Bearer must be an Orc.<br>Bearer is <b>fierce</b>.<br>If bearer is Yazneg, he is strength +3 and <b>damage +1</b>.<br><b>Regroup:</b> Exert bearer twice to discard a Free Peoples possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Scouting Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedOnAnyOrcItMakesThatOrcFierceAndAnOrcWhoIsNotYaznegNoStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var yazneg = scn.GetShadowCard("yazneg");
		var footman = scn.GetShadowCard("footman");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveMinionsToTable(yazneg, footman, veteran, bats);
		scn.MoveCardsToHand(warg);
		scn.SetTwilight(10);
		assertInHand(warg);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(footman, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(yazneg));

		assertTrue(scn.ShadowPlayAvailable(warg));
		scn.ShadowPlayCard(warg);
		// Orcs of three cultures; not Dark Bats, a minion who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(yazneg, footman, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(footman);

		assertAttachedTo(warg, footman);
		assertTrue(scn.HasKeyword(footman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(veteran, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		// 8 + 3 for the Warg, and no more: the Footman is no Yazneg. Yazneg, who bears nothing, is untouched.
		assertEquals(11, scn.GetStrength(footman));
		assertEquals(0, scn.GetKeywordCount(footman, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(0, scn.GetKeywordCount(yazneg, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void YaznegBearingItIsStrengthPlus3AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var yazneg = scn.GetShadowCard("yazneg");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(yazneg, footman);
		scn.AttachCardsTo(footman, warg2);
		assertAttachedTo(warg2, footman);
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(0, scn.GetKeywordCount(yazneg, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));
		assertEquals(11, scn.GetStrength(footman));

		scn.AttachCardsTo(yazneg, warg);
		// 5 + 3 for the Warg + 3 for being Yazneg.
		assertEquals(11, scn.GetStrength(yazneg));
		assertEquals(1, scn.GetKeywordCount(yazneg, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(yazneg, Keyword.FIERCE));
		// The other copy's bearer is no Yazneg, and this copy's bonus is its bearer's alone.
		assertEquals(11, scn.GetStrength(footman));
		assertEquals(0, scn.GetKeywordCount(footman, Keyword.DAMAGE));
	}

	@Test
	public void RegroupActionExertsBearerTwiceToDiscardAFreePeoplesPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var veteran = scn.GetShadowCard("veteran");
		var footman = scn.GetShadowCard("footman");
		var scimitar = scn.GetShadowCard("scimitar");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var toby = scn.GetFreepsCard("toby");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		scn.MoveCompanionsToTable(dwalin, thorin);
		scn.AttachCardsTo(dwalin, axe);
		scn.AttachCardsTo(thorin, orcrist);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, footman);
		scn.AttachCardsTo(veteran, warg);
		scn.AttachCardsTo(footman, warg2, scimitar);
		assertAttachedTo(axe, dwalin);
		assertAttachedTo(orcrist, thorin);
		assertInZone(Zone.SUPPORT, toby);
		assertAttachedTo(scimitar, footman);

		// Both Orcs stand unassigned into the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(footman));

		assertTrue(scn.ShadowActionAvailable(warg));
		scn.ShadowUseCardAction(warg);
		// This copy's bearer exerts twice; the other copy's bearer does not.
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(footman));

		// A borne [dwarven] possession and a [shire] one in the support area; not the Free Peoples artifact Orcrist,
		// nor the Shadow possessions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, toby));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orcrist, scimitar, warg2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);

		assertInZone(Zone.DISCARD, axe);
		assertInZone(Zone.SUPPORT, toby);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(scimitar, footman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenBearerCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var veteran = scn.GetShadowCard("veteran");
		var footman = scn.GetShadowCard("footman");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(dwalin);
		scn.AttachCardsTo(dwalin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, footman);
		scn.AttachCardsTo(veteran, warg);
		scn.AttachCardsTo(footman, warg2);
		// Vitality 2 + 2 for the Warg, 2 wounds: 2 left, one short of exerting twice.
		scn.AddWoundsToChar(veteran, 2);
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertAttachedTo(axe, dwalin);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertFalse(scn.ShadowActionAvailable(warg));
		// The other copy, whose bearer (vitality 1 + 2) can exert twice, is offered.
		assertTrue(scn.ShadowActionAvailable(warg2));
		assertAttachedTo(axe, dwalin);
	}

	@Test
	public void WithNoFreePeoplesPossessionTheBearerExertsTwiceForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var veteran = scn.GetShadowCard("veteran");
		var footman = scn.GetShadowCard("footman");
		var scimitar = scn.GetShadowCard("scimitar");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, orcrist);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, footman);
		scn.AttachCardsTo(veteran, warg);
		scn.AttachCardsTo(footman, scimitar);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(scimitar, footman);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(veteran));

		// Only a Free Peoples artifact and a Shadow possession: offered, paid, and nothing discarded.
		assertTrue(scn.ShadowActionAvailable(warg));
		scn.ShadowUseCardAction(warg);
		assertEquals(2, scn.GetWoundsOn(veteran));
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(scimitar, footman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
