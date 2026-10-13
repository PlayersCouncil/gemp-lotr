package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_096_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("night", "103_96");
					put("sky1", "103_97");        // Ominous Sky: a [sauron] twilight condition, cost 2, nothing needed to play it
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear: a [ringwraith] twilight condition, cost 2; to play, hinder 2 twilight conditions
					put("despair", "103_93");     // Cover of Darkness, Omen of Despair: a [sauron] twilight condition
					put("dread", "103_94");       // Cover of Darkness, Omen of Dread: a [sauron] twilight condition; its responses answer burdens and threats, of which these tests add none
					put("wreathed", "1_238");     // Wreathed in Shadow: a [ringwraith] condition that is not twilight; an archery ability
					put("armory", "1_173");       // Goblin Armory: a [moria] condition, cost 0, nothing needed to play it
					put("oath", "17_66");         // Wildman's Oath: a [men] possession that plays to the support area
					put("poison", "8_28");        // Spider Poison: a Shadow possession a companion bears; it does nothing unless its bearer is assigned to a skirmish
					put("stairs", "1_24");        // Stairs of Khazad-dum: a Free Peoples condition in the support area

					put("sauron", "9_48");        // Sauron, The Lord of the Rings: cost 18 (less for burdens, threats and sites controlled: there are none), site 6
					put("sauronV", "103_100");    // Sauron, Lord of All Middle-earth: a [sauron] Maia with no keyword of its own below 6 companions
					put("mouth", "12_118");       // The Mouth of Sauron, Lieutenant of Barad-dur: a [sauron] Man whose title is not Sauron

					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc, no keywords
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc, no text
					put("udun", "6_106");         // Troll of Udun: a [sauron] Troll; fierce
					put("cavetroll", "1_165");    // Cave Troll of Moria: a [moria] Troll; damage +1, fierce
					put("lemenya", "1_232");      // Ulaire Lemenya, Lieutenant of Morgul: a [ringwraith] Nazgul, no keywords
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: a [raider] Nazgul; Easterling, fierce
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: a twilight card that is no condition; a Nazgul
					put("explorer", "4_250");     // Southron Explorer: a [raider] Man
					put("savage", "1_151");       // Uruk Savage: an [isengard] Uruk-hai; damage +1
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] Uruk-hai; damage +1

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man who is the Free Peoples player's
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard
					put("sleep", "1_84");         // Sleep, Caradhras: "Exert Gandalf to discard every condition."
					put("sleepErrata", "51_84");  // Sleep, Caradhras (errata): "Exert Gandalf to discard a Shadow condition and then hinder all other conditions."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EndlessNightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Endless Night
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight. To play, hinder 4 twilight conditions. This cannot be discarded or hindered. Orcs gain <b>fierce</b>. Trolls gain <b>enduring</b>. Nazgul gain <b>damage +1</b>. Your Men and Uruk-hai gain <b>archer</b>.
		 * 	Shadow: Hinder X of your other Shadow support cards to play Sauron from your hand or discard pile; he is twilight cost -X.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("night");

		assertEquals("Endless Night", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForFiveByHinderingFourTwilightConditionsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var fear = scn.GetShadowCard("fear");
		var despair = scn.GetShadowCard("despair");
		var wreathed = scn.GetShadowCard("wreathed");
		var nelya = scn.GetShadowCard("nelya");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(night);
		// Three Ominous Skies (three dots) and two Covers of Darkness (two dots, whatever their subtitles): the
		// five twilight conditions that are as many as a game can have in play beside this one (CR 5.0
		// "non-unique").
		scn.MoveCardsToSupportArea(sky1, sky2, sky3, fear, despair, wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(night));

		scn.ShadowPlayCard(night);

		// Five twilight conditions of two cultures may be hindered. Not offered: a condition that is not
		// twilight, and a twilight card that is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, sky2, sky3, fear, despair));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wreathed, nelya));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(sky1, sky2, fear, despair);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(fear));
		assertTrue(scn.IsHindered(despair));
		assertFalse(scn.IsHindered(sky3));
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(nelya));
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, night);
		assertFalse(scn.IsHindered(night));
		// With it in play the Orc is fierce.
		assertTrue(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFewerThanFourTwilightConditionsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var fear = scn.GetShadowCard("fear");
		var dread = scn.GetShadowCard("dread");
		var wreathed = scn.GetShadowCard("wreathed");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(night, sky3);
		scn.MoveCardsToSupportArea(sky1, sky2, fear, dread, wreathed);
		scn.HinderCard(dread);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Three twilight conditions that can be hindered, beside one that already is, a condition that is not
		// twilight and a twilight minion.
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(night));

		scn.ShadowPlayCard(sky3);
		assertTrue(scn.ShadowPlayAvailable(night));

		scn.ShadowPlayCard(night);

		// Exactly four can be hindered, so they are, without a choice.
		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(sky3));
		assertTrue(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(nelya));
		assertInZone(Zone.SUPPORT, night);
		// 13 - 2 for the Sky - 5
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysInPlayWhenEveryConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var wreathed = scn.GetShadowCard("wreathed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToSupportArea(night, sky1, wreathed);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, night, sky1, wreathed);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(sleep);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(sky1, wreathed);
		assertInZone(Zone.SUPPORT, night);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedForDiscardAndIsNotHinderedWithEveryOtherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var wreathed = scn.GetShadowCard("wreathed");
		var armory = scn.GetShadowCard("armory");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleepErrata");
		scn.MoveCardsToSupportArea(night, sky1, wreathed, armory);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		scn.FreepsPlayCard(sleep);

		// "Discard a Shadow condition": every Shadow condition but this one may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sky1, wreathed, armory));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(night));
		scn.FreepsChooseCard(wreathed);

		assertInDiscard(wreathed);
		// "...and then hinder all other conditions."
		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(night));
		assertInZone(Zone.SUPPORT, night);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBeHinderedToPayForAnotherTwilightCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(night, sky1);
		scn.MoveCardsToHand(fear, sky2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Cover of Darkness: "To play, hinder 2 twilight conditions." Endless Night and one Ominous Sky are in
		// play, and only the Sky can be hindered.
		assertFalse(scn.ShadowPlayAvailable(fear));

		scn.ShadowPlayCard(sky2);
		assertTrue(scn.ShadowPlayAvailable(fear));

		scn.ShadowPlayCard(fear);

		// The two Skies are the only two that can pay, so they do, without a choice.
		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(night));
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachRaceGainsItsOwnKeywordAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var udun = scn.GetShadowCard("udun");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var explorer = scn.GetShadowCard("explorer");
		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var sauron = scn.GetShadowCard("sauronV");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop, udun, cavetroll, lemenya, khamul, explorer, mouth, savage, sentry, sauron);

		// Without it: none of the keywords it gives. (Both Trolls and Khamul are fierce in print, and the
		// Uruk-hai and the Cave Troll damage +1.)
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(udun, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(cavetroll, Keyword.ENDURING));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(explorer, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(mouth, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(savage, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sentry, Keyword.ARCHER));

		scn.MoveCardsToSupportArea(night);

		// Orcs, of two cultures, are fierce; nobody else has become so.
		assertTrue(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(mouth, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(aragorn, Keyword.FIERCE));

		// Trolls, of two cultures, are enduring.
		assertTrue(scn.HasKeyword(udun, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(cavetroll, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(soldier, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(troop, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(khamul, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(explorer, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(mouth, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(savage, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(sentry, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(sauron, Keyword.ENDURING));

		// Nazgul, of two cultures, are damage +1; those who were so already have gained nothing.
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(udun, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(cavetroll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(mouth, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));

		// The Shadow player's Men and Uruk-hai, of two cultures each, are archers. Aragorn is a Man and not
		// that player's; Khamul is an Easterling and no Man.
		assertTrue(scn.HasKeyword(explorer, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(mouth, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(savage, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(sentry, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(udun, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(cavetroll, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lemenya, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(khamul, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sauron, Keyword.ARCHER));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowActionHindersTwoOtherShadowSupportCardsToPlaySauronFromTheDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var wreathed = scn.GetShadowCard("wreathed");
		var oath = scn.GetShadowCard("oath");
		var poison = scn.GetShadowCard("poison");
		var soldier = scn.GetShadowCard("soldier");
		var shadowStairs = scn.GetShadowCard("stairs");
		var freepsStairs = scn.GetFreepsCard("stairs");
		var freepsArmory = scn.GetFreepsCard("armory");
		var sauron = scn.GetShadowCard("sauron");
		var sauronV = scn.GetShadowCard("sauronV");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToSupportArea(night, sky1, sky2, wreathed, oath, shadowStairs, freepsStairs, freepsArmory);
		scn.HinderCard(sky2);
		scn.MoveCardsToDiscard(sauron, sauronV, mouth);

		scn.StartGame();
		scn.AttachCardsTo(scn.GetRingBearer(), poison);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(22);
		scn.FreepsPass();

		// 22 set + 1 companion + 2 for the site
		assertEquals(25, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(night));

		scn.ShadowUseCardAction(night);

		// Offered: a [sauron] and a [ringwraith] condition and a [men] possession in the Shadow player's support
		// area. Not offered: Endless Night itself, a support card already hindered, a minion, a Shadow
		// possession a companion bears, a Free Peoples condition in either player's support area, and the
		// other player's Shadow condition in his support area (not "your", and not active on his own turn).
		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, wreathed, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(night, sky2, soldier, poison, shadowStairs, freepsStairs, freepsArmory));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(sky1, oath);

		// Either Sauron in the discard pile may be played; The Mouth of Sauron is a [sauron] minion and not Sauron.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sauron, sauronV));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mouth));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(sauron);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(oath));
		assertFalse(scn.IsHindered(wreathed));
		assertFalse(scn.IsHindered(freepsArmory));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertInDiscard(sauronV, mouth);
		// 18 - 2 for the two cards hindered + 2 roaming (site 6, at site 2)
		assertEquals(25 - 18, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolToPayInFullNothingNeedBeHinderedAndSauronIsPlayedFromHandAtFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sauron = scn.GetShadowCard("sauron");
		var sauronV = scn.GetShadowCard("sauronV");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToSupportArea(night, sky1);
		scn.MoveCardsToHand(sauron, sauronV, mouth);

		scn.StartGame();
		scn.SetTwilight(17);
		scn.FreepsPass();

		// 17 set + 1 companion + 2 for the site: exactly 18 + 2 roaming
		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(night));

		scn.ShadowUseCardAction(night);

		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowDeclineChoosing();

		// Either Sauron in hand may be played; The Mouth of Sauron, a [sauron] minion the pool could pay for
		// (3 + 2 roaming), is not Sauron.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sauron, sauronV));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mouth));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(sauron);

		assertFalse(scn.IsHindered(sky1));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertInZone(Zone.HAND, sauronV, mouth);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithASauronInHandAndOneInTheDiscardPileThePlayerChoosesWhichToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sauron = scn.GetShadowCard("sauron");
		var sauronV = scn.GetShadowCard("sauronV");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToSupportArea(night, sky1);
		scn.MoveCardsToHand(sauronV, mouth);
		scn.MoveCardsToDiscard(sauron);

		scn.StartGame();
		scn.SetTwilight(22);
		scn.FreepsPass();

		assertEquals(25, scn.GetTwilight());

		scn.ShadowUseCardAction(night);
		scn.ShadowChooseCards(sky1);

		assertTrue(scn.ShadowChoiceAvailable("from hand"));
		assertTrue(scn.ShadowChoiceAvailable("from your discard pile"));
		scn.ShadowChooseOption("from your discard pile");

		// One Sauron is there, and is played without a further choice.
		assertTrue(scn.IsHindered(sky1));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertInZone(Zone.HAND, sauronV, mouth);
		// 18 - 1 for the card hindered + 2 roaming
		assertEquals(25 - 19, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithSauronInNeitherHandNorDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var armory = scn.GetShadowCard("armory");
		var sauron = scn.GetShadowCard("sauron");
		var mouth = scn.GetShadowCard("mouth");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(night, sky1);
		// [sauron] minions in hand and in the discard pile, Sauron in the Shadow player's draw deck and in the
		// other player's discard pile.
		scn.MoveCardsToHand(mouth, armory);
		scn.MoveCardsToDiscard(soldier);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("sauron"), scn.GetFreepsCard("sauronV"));

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertInZone(Zone.DECK, sauron);
		assertFalse(scn.ShadowActionAvailable(night));

		// With Sauron in the discard pile, the next decision offers it.
		scn.MoveCardsToDiscard(sauron);
		scn.ShadowPlayCard(armory);
		assertTrue(scn.ShadowActionAvailable(night));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedUntilHinderingEveryOtherSupportCardBringsSauronInHandWithinThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var armory = scn.GetShadowCard("armory");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(night, sky1);
		scn.MoveCardsToHand(sauron, armory);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		// Sauron is 18 + 2 roaming. One card can be hindered: 19, one more than the pool.
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(night));

		// A second support card, played for nothing: 18, exactly the pool.
		scn.ShadowPlayCard(armory);
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(night));

		// Both must be hindered to bring Sauron within the pool, so there is nothing to choose.
		scn.ShadowUseCardAction(night);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(armory));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedUntilHinderingEveryOtherSupportCardBringsSauronInTheDiscardPileWithinThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var armory = scn.GetShadowCard("armory");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(night, sky1);
		scn.MoveCardsToHand(armory);
		scn.MoveCardsToDiscard(sauron);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		// Sauron is 18 + 2 roaming. One card can be hindered: 19, one more than the pool.
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(night));

		// A second support card, played for nothing: 18, exactly the pool.
		scn.ShadowPlayCard(armory);
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(night));

		// Both must be hindered to bring Sauron within the pool, so there is nothing to choose.
		scn.ShadowUseCardAction(night);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(armory));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFewestCardsThatMayBeHinderedAreAsManyAsBringSauronWithinThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var armory = scn.GetShadowCard("armory");
		var oath = scn.GetShadowCard("oath");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(night, sky1, armory, oath);
		scn.MoveCardsToDiscard(sauron);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		// Sauron is 18 + 2 roaming and the pool is 18: X has to be at least 2, and can be 3.
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(night));

		scn.ShadowUseCardAction(night);

		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, armory, oath));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(sky1, oath);

		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
