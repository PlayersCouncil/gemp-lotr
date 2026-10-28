package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_159_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weapon", "12_159");
					put("squad", "11_202");      // Squad of Uruk-hai: [uruk-hai] Uruk-hai, twilight cost 4; a damage bonus against low resistance only
					put("patrol", "11_198");     // Patrol of Uruk-hai: [uruk-hai] Uruk-hai, twilight cost 4; a wound bar in a skirmish only
					put("bloodthirsty", "11_178"); // Bloodthirsty Uruk: [uruk-hai] Uruk-hai, twilight cost 3; stronger beside an exhausted companion only
					put("saruman", "17_116");    // Saruman, Master of the White Hand: [uruk-hai] Wizard minion, twilight cost 4
					put("guard", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai; Damage +1 is its whole text
					put("alliance", "12_141");   // Dark Alliance: [uruk-hai] support-area condition; a response only
					put("spear", "17_117");      // Spear of the White Hand: [uruk-hai] hand weapon, cost 0, borne by an [uruk-hai] Uruk-hai; no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeaponofOpportunityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Weapon of Opportunity
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be an [uruk-hai] minion.
		 * When you play this possession on a minion that has a twilight cost of 4 or more, draw a card.
		*/

		var scn = GetScenario();

		var weapon = scn.GetFreepsCard("weapon");

		assertEquals("Weapon of Opportunity", weapon.getBlueprint().getTitle());
		assertNull(weapon.getBlueprint().getSubtitle());
		assertFalse(weapon.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, weapon.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, weapon.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, weapon.getBlueprint().getCardType());
		assertTrue(weapon.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, weapon.getBlueprint().getTwilightCost());
		assertEquals(2, weapon.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnUrukHaiMinionAndDrawsNothingOnOneOfTwilightCost3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapon = scn.GetShadowCard("weapon");
		var squad = scn.GetShadowCard("squad");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(weapon);
		scn.MoveCardsToSupportArea(alliance);
		assertInHand(weapon);
		assertInZone(Zone.SUPPORT, alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, bloodthirsty, saruman, guard);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowPlayAvailable(weapon));
		scn.ShadowPlayCard(weapon);
		// [uruk-hai] minions of two races are offered; an Uruk-hai of another culture and an [uruk-hai] card that is
		// no minion are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(squad, bloodthirsty, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, alliance));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bloodthirsty);

		assertEquals(bloodthirsty, weapon.getAttachedTo());
		// Twilight cost 3, one short: the Weapon leaves the hand and nothing is drawn.
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedOnAMinionOfTwilightCost4ItDrawsACardAndAnotherPossessionPlayedAfterItDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapon = scn.GetShadowCard("weapon");
		var spear = scn.GetShadowCard("spear");
		var squad = scn.GetShadowCard("squad");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(weapon, spear);
		assertInHand(weapon, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(squad, patrol);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(weapon);
		scn.ShadowChooseCard(squad);
		assertEquals(squad, weapon.getAttachedTo());
		// The Weapon leaves the hand and a card is drawn in its place.
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		// The Spear goes on the other Uruk-hai of cost 4 (the Squad bears a hand weapon already): nothing is drawn.
		scn.ShadowPlayCard(spear);
		assertEquals(patrol, spear.getAttachedTo());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
