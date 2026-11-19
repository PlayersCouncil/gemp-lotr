package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dagger1", "18_69");
					put("dagger2", "18_69");
					put("dagger3", "18_69");
					put("dagger4", "18_69");
					put("poleaxe1", "11_95");   // Poleaxe: a [men] hand weapon for a [men] minion; its skirmish ability is used only on purpose
					put("poleaxe2", "11_95");
					put("trident", "12_77");    // War Trident: a [men] hand weapon for a [men] minion; its bonuses are only in a skirmish
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil
					put("squad", "11_99");      // Squad of Haradrim: a [men] Man minion; its regroup ability is used only on purpose
					put("bearer", "11_84");     // Harad Standard-bearer: a [men] Man minion; its skirmish ability is used only on purpose
					put("ted", "18_75");        // Ted Sandyman: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("soldier", "4_220");    // Desert Soldier: a [raider] Man minion; his trigger is at the start of a skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples Man companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HenchmansDaggerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Henchman's Dagger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [men] minion.
		 * Bearer may bear this hand weapon in addition to 1 other hand weapon.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dagger1");

		assertEquals("Henchman's Dagger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnAMenMinionOfAnyRaceAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger1 = scn.GetShadowCard("dagger1");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(dagger1);

		scn.StartGame();
		// Laid in the fellowship phase, to stand in the Shadow phase that follows.
		scn.MoveMinionsToTable(legion, ted, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(dagger1);
		// A [men] Man and a [men] Hobbit; not a [raider] Man minion, nor a Free Peoples Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertAttachedTo(dagger1, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MayBeBorneBesideOneOtherHandWeaponWhicheverComesFirstButNotBesideTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger1 = scn.GetShadowCard("dagger1");
		var dagger2 = scn.GetShadowCard("dagger2");
		var dagger3 = scn.GetShadowCard("dagger3");
		var dagger4 = scn.GetShadowCard("dagger4");
		var poleaxe1 = scn.GetShadowCard("poleaxe1");
		var poleaxe2 = scn.GetShadowCard("poleaxe2");
		var trident = scn.GetShadowCard("trident");
		var legion = scn.GetShadowCard("legion");
		var squad = scn.GetShadowCard("squad");
		var bearer = scn.GetShadowCard("bearer");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(dagger2, dagger3, dagger4, poleaxe2);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, squad, bearer, ted);
		scn.AttachCardsTo(legion, poleaxe1);
		scn.AttachCardsTo(ted, dagger1);
		scn.AttachCardsTo(bearer, trident);
		assertAttachedTo(poleaxe1, legion);
		assertAttachedTo(dagger1, ted);
		assertAttachedTo(trident, bearer);
		// The Poleaxe costs 1; the daggers cost nothing.
		scn.SetTwilight(5);
		scn.FreepsPass();

		// A dagger beside another hand weapon (Legion's Poleaxe), beside another dagger (Ted's), beside one with no
		// "in addition" text (the Standard-bearer's Trident), and on a minion with none.
		scn.ShadowPlayCard(dagger2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, ted, bearer, squad));
		scn.ShadowChooseCard(legion);
		assertAttachedTo(dagger2, legion);
		assertAttachedTo(poleaxe1, legion);

		// The other way round: a plain hand weapon may join a dagger already borne.  Legion, with two hand weapons,
		// has no room; nor has the Standard-bearer, whose one hand weapon is no dagger, while Squad is offered.
		scn.ShadowPlayCard(poleaxe2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(ted, squad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legion, bearer));
		scn.ShadowChooseCard(ted);
		assertAttachedTo(poleaxe2, ted);
		assertAttachedTo(dagger1, ted);

		// A dagger is borne beside 1 other hand weapon, not 2: Legion and Ted each bear two already.
		scn.ShadowPlayCard(dagger3);
		assertTrue(scn.ShadowHasCardChoiceAvailable(squad, bearer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legion, ted));
		scn.ShadowChooseCard(squad);
		assertAttachedTo(dagger3, squad);

		// A second dagger is the 1 other hand weapon beside the first.
		scn.ShadowPlayCard(dagger4);
		assertTrue(scn.ShadowHasCardChoiceAvailable(squad, bearer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legion, ted));
		scn.ShadowChooseCard(squad);
		assertAttachedTo(dagger4, squad);
		assertAttachedTo(dagger3, squad);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
