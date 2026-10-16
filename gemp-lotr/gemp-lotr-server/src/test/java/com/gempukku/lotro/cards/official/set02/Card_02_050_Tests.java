package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "2_50");
					put("balrog", "12_79");   // The Balrog, The Terror of Khazad-dum: damage +1; the rest of his text acts only underground
					put("spear1", "1_182");   // Goblin Spear: a hand weapon for a [moria] minion, with no "in addition" text
					put("spear2", "1_182");
					put("whip", "2_74");      // Whip of Many Thongs: a hand weapon for The Balrog, with no "in addition" text
					put("troll", "1_165");    // Cave Troll of Moria: a [moria] minion who is no Balrog, printed damage +1
					put("troop1", "1_177");   // Goblin Patrol Troop: a [moria] minion with no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: The Balrog's Sword
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: +3
		 * Game Text: Bearer must be The Balrog.
		 * It is <b>damage +1</b>.
		 * This weapon may be borne in addition to 1 other hand weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("The Balrog's Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnTheBalrogBesideTheHandWeaponHeBearsItMakesHimDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var balrog = scn.GetShadowCard("balrog");
		var spear = scn.GetShadowCard("spear1");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, troll);
		scn.AttachCardsTo(balrog, spear);
		scn.SetTwilight(10);
		assertAttachedTo(spear, balrog);
		assertEquals(1, scn.GetKeywordCount(balrog, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sword));

		// The Balrog is the only bearer it takes, so it goes on him unasked; the Troll is no Balrog.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, balrog);
		assertAttachedTo(spear, balrog);
		// "It" is The Balrog: his printed damage +1, and +1 from the Sword.  The Troll beside him is untouched.
		assertEquals(2, scn.GetKeywordCount(balrog, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedOnAMoriaMinionWhoIsNotTheBalrog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The pool would pay for it many times over; only the bearer is missing.
		// 10 laid + 2 for Ettenmoors + 1 companion
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(sword));
	}

	@Test
	public void MayBeBorneBesideOneOtherHandWeaponWhicheverComesFirstButNotBesideTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var balrog = scn.GetShadowCard("balrog");
		var spear1 = scn.GetShadowCard("spear1");
		var spear2 = scn.GetShadowCard("spear2");
		var whip = scn.GetShadowCard("whip");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToHand(spear1, spear2, whip);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, troop1, troop2);
		scn.AttachCardsTo(balrog, sword);
		scn.SetTwilight(10);
		assertAttachedTo(sword, balrog);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Beside the Sword alone, The Balrog has room for another hand weapon: the Whip can go on him.
		assertTrue(scn.ShadowPlayAvailable(whip));

		// A plain hand weapon joins the Sword already borne: The Balrog is offered with the two Troops.
		scn.ShadowPlayCard(spear1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(balrog, troop1, troop2));
		scn.ShadowChooseCard(balrog);
		assertAttachedTo(spear1, balrog);
		assertAttachedTo(sword, balrog);

		// Borne beside 1 other hand weapon, not 2: The Balrog has no room left, while the Troops are offered in
		// the same choice; and the Whip, which only he could bear, can no longer be played.
		assertFalse(scn.ShadowPlayAvailable(whip));
		scn.ShadowPlayCard(spear2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, troop2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balrog));
		scn.ShadowChooseCard(troop1);
		assertAttachedTo(spear2, troop1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
