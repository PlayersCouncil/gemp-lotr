package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The tokens on a hindered card: wounds and culture tokens.
 *
 * Comprehensive Rules 5.0, "hinder": "Tokens on hindered cards cannot be spotted, counted, reinforced, removed, or
 * added to (even if there are zero tokens).  Hindered characters thus cannot be exerted, wounded, or healed."  And
 * under it: "If a character is hindered before it is about to be wounded, exerted, or healed, then the wound,
 * exertion, or heal does not resolve.  Example: Anduril, Legend Remade hinders a companion as it is about to be
 * wounded.  Because that companion is now hindered, the wound token cannot be added."
 *
 * (The one exception the entry makes, the burdens of a hindered Ring-bearer, is HinderedRingBearerTests'.)
 *
 * Except where Anduril does it, the hindering is only how the board is laid, and is done by the rig; the reading
 * is a real card failing to exert, wound, heal, spot, count, reinforce or remove on the hindered card what it
 * does on the cards beside it.
 */
public class TokensOnAHinderedCardTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability never used
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; a fellowship ability never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability never used

					// Desert Lord: [raider] Man, vitality 3, archer.  "Archery: Exert Desert Lord to exert a
					// companion (except the Ring-bearer); Desert Lord does not add to the minion archery total."
					put("lord", "4_219");

					// Worth Fighting For: [shire] Fellowship event.  "To play, spot a Hobbit companion.  Make the
					// Shadow player exhaust 1 of your characters.  For each exertion made, hinder a Shadow support
					// card. ..."  No Shadow card lies in a support area here, and the last sentence asks nothing
					// while the Shadow player has no hindered card.
					put("worth1", "103_120");
					put("worth2", "103_120");

					// Anduril, Legend Remade: on Aragorn.  "Response: If an unbound companion (except Aragorn)
					// is about to take a wound, add 2 threats to hinder that companion.  Add (2) unless that
					// companion was [gondor] or a Man."
					put("anduril", "103_18");
					put("marksman1", "1_176");  // Goblin Marksman: "Archer" is its whole text
					put("marksman2", "1_176");

					// Master of Healing: [elven] event.  "Fellowship or Regroup: Spot Elrond (or 2 [elven]
					// allies) to heal a companion or ally."
					put("healing1", "3_22");
					put("healing2", "3_22");
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, never wounded here

					// Morgul Whelp: strength 3.  "While you can spot a wound on the Ring-bearer, this minion is
					// strength +6."
					put("whelp", "7_202");
					// Bloodthirsty Uruk: strength 8.  "While you can spot an exhausted companion, this minion is
					// strength +3."
					put("uruk", "11_178");
					// Corsair Champion: "While each companion is wounded, this minion is fierce."  Its other
					// text is used from hand.
					put("champion", "13_84");

					// Ships of Great Draught: [raider] possession in the support area that carries [raider]
					// tokens; its two abilities are never used.
					put("ships1", "8_65");
					put("ships2", "8_65");
					put("ships3", "8_65");
					// Desert Wind: "While you can spot 5 or more culture tokens, the minion archery total is
					// +2."  No archer.
					put("wind", "13_86");

					// Ranger of the South: [gondor] Man, strength 5.  "While you can spot a [gondor] token, this
					// companion is strength +2."
					put("ranger", "18_56");
					// Three [gondor] conditions of the support area that carry [gondor] tokens.  Each has a
					// skirmish ability, never used.
					put("leaders", "7_112");    // Noble Leaders
					put("heirs", "13_69");      // Heirs of Gondor
					put("garrison", "6_52");    // Garrison of Osgiliath

					// Brego, Loyal Steed: mount for a [gondor] companion.  "When you play Brego, you may
					// reinforce a [gondor] token.  Maneuver: Exert bearer and remove 2 [gondor] tokens to wound a
					// minion."
					put("brego", "13_63");
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text

					// Hidden Knowledge: [gondor] Fellowship event.  "Spot a [gondor] companion to reveal the top
					// 5 cards of an opponent's draw deck.  For each Shadow card revealed, add a threat.  For each
					// Free Peoples card revealed, place a [gondor] token on one of your conditions with a
					// [gondor] token on it.  Shuffle that draw deck."
					put("knowledge1", "7_104");
					put("knowledge2", "7_104");
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: vitality 4; his fellowship ability needs a companion with his signet
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCharacterIsNotExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetShadowCard("lord");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(lord);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// With nobody hindered each companion but the Ring-bearer may be made to exert.
		assertEquals(0, scn.GetWoundsOn(lord));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.ShadowUseCardAction(lord);
		assertEquals(1, scn.GetWoundsOn(lord));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.HinderCard(aragorn);
		scn.FreepsPass();

		// Hindered, Aragorn is not there to be exerted.  The other two still are, and the Lord has paid again.
		assertTrue(scn.IsHindered(aragorn));
		scn.ShadowUseCardAction(lord);
		assertEquals(2, scn.GetWoundsOn(lord));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.RestoreCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void AHinderedCharacterIsNotExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var worth1 = scn.GetFreepsCard("worth1");
		var worth2 = scn.GetFreepsCard("worth2");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveCardsToHand(worth1, worth2);

		scn.StartGame();

		// With nobody hindered the Shadow player may exhaust any of the four.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsPlayCard(worth1);
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, sam, gimli));
		scn.ShadowChooseCard(sam);
		// Vitality 4: exhausted by three exertions.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.HinderCard(aragorn);

		// Hindered, Aragorn is not there to be exhausted; nor is Sam, who cannot exert again.
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(worth2);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, sam));
		scn.ShadowChooseCard(gimli);
		// Vitality 3: exhausted by two exertions.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void AHinderedCharacterTakesNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var anduril = scn.GetFreepsCard("anduril");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		// Two archers: two archery wounds for the Free Peoples player to place.
		scn.SkipToArcheryWounds();

		// The first may go to any of the three companions.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(gimli);

		// The rules' own example: Gimli is hindered as he is about to be wounded, and the wound is not placed.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertFalse(scn.IsHindered(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The second wound finds him hindered already: he is not one of those it may go to.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The wound that was stopped does not come back when he is restored.
		scn.RestoreCard(gimli);
		assertFalse(scn.IsHindered(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
	}

	@Test
	public void AHinderedCharacterIsNotHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var healing2 = scn.GetFreepsCard("healing2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1, healing2);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 2);

		// With nobody hindered each of the three wounded companions may be healed.
		scn.FreepsPlayCard(healing1);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.HinderCard(aragorn);

		// Hindered, Aragorn is not there to be healed, though he still has wounds.  The other two are.
		assertTrue(scn.IsHindered(aragorn));
		scn.FreepsPlayCard(healing2);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void AWoundOnAHinderedCharacterIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(whelp);

		assertEquals(3, scn.GetStrength(whelp));
		// A wound on another companion is not one on the Ring-bearer.
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(3, scn.GetStrength(whelp));
		scn.AddWoundsToChar(frodo, 1);
		// 3 + 6
		assertEquals(9, scn.GetStrength(whelp));

		// Hindering the other companion takes nothing from the spot.
		scn.HinderCard(aragorn);
		assertEquals(9, scn.GetStrength(whelp));
		scn.RestoreCard(aragorn);

		// The wound is still on the hindered Ring-bearer, and is not there to be spotted.
		scn.HinderCard(frodo);
		assertTrue(scn.IsHindered(frodo));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(whelp));

		scn.RestoreCard(frodo);
		assertEquals(9, scn.GetStrength(whelp));
	}

	@Test
	public void AHinderedCompanionIsNeitherWoundedNorExhaustedToACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var uruk = scn.GetShadowCard("uruk");
		var champion = scn.GetShadowCard("champion");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// The Ring-bearer is wounded, Aragorn (vitality 4) is exhausted, and Gimli has no wound.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(uruk, champion);

		assertTrue(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(frodo));
		// 8 + 3 for the exhausted companion
		assertEquals(11, scn.GetStrength(uruk));
		// Not each companion is wounded.
		assertFalse(scn.HasKeyword(champion, Keyword.FIERCE));

		// Hindered, Aragorn is no exhausted companion to spot.  Gimli still stands without a wound.
		scn.HinderCard(aragorn);
		assertEquals(8, scn.GetStrength(uruk));
		assertFalse(scn.HasKeyword(champion, Keyword.FIERCE));

		scn.RestoreCard(aragorn);
		assertEquals(11, scn.GetStrength(uruk));

		// Hindered, Gimli is no companion to be found without a wound: each companion there is to spot, the
		// Ring-bearer and Aragorn, is wounded.  Aragorn is still exhausted.
		scn.HinderCard(gimli);
		assertTrue(scn.HasKeyword(champion, Keyword.FIERCE));
		assertEquals(11, scn.GetStrength(uruk));

		scn.RestoreCard(gimli);
		assertFalse(scn.HasKeyword(champion, Keyword.FIERCE));
	}

	@Test
	public void CultureTokensOnAHinderedCardAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var ships3 = scn.GetShadowCard("ships3");
		var wind = scn.GetShadowCard("wind");
		scn.MoveCardsToSupportArea(ships1, ships2, ships3);

		scn.StartGame();
		scn.MoveMinionsToTable(wind);

		assertEquals(0, scn.GetShadowArcheryTotal());
		// Four tokens are one short.
		scn.AddTokensToCard(ships1, 3);
		scn.AddTokensToCard(ships2, 1);
		assertEquals(0, scn.GetShadowArcheryTotal());
		// Five, on two cards: 3 and 2.
		scn.AddTokensToCard(ships2, 1);
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Hindering the card that has no token takes nothing from the count.
		scn.HinderCard(ships3);
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.RestoreCard(ships3);

		// The two tokens on a hindered card are not counted: three are left.
		scn.HinderCard(ships2);
		assertTrue(scn.IsHindered(ships2));
		assertEquals(2, scn.GetCultureTokensOn(ships2));
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.RestoreCard(ships2);
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Nor are the three: two are left.
		scn.HinderCard(ships1);
		assertEquals(3, scn.GetCultureTokensOn(ships1));
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.RestoreCard(ships1);
		assertEquals(2, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ACultureTokenOnAHinderedCardIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToSupportArea(leaders, heirs);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(ranger));
		scn.AddTokensToCard(leaders, 1);
		// 5 + 2
		assertEquals(7, scn.GetStrength(ranger));

		// Hindering the condition that has no token takes nothing from the spot.
		scn.HinderCard(heirs);
		assertEquals(7, scn.GetStrength(ranger));
		scn.RestoreCard(heirs);

		// The only [gondor] token is on a hindered card, and is not there to be spotted.
		scn.HinderCard(leaders);
		assertTrue(scn.IsHindered(leaders));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(5, scn.GetStrength(ranger));

		scn.RestoreCard(leaders);
		assertEquals(7, scn.GetStrength(ranger));
	}

	@Test
	public void NoCultureTokenIsReinforcedOnAHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		var garrison = scn.GetFreepsCard("garrison");
		var brego = scn.GetFreepsCard("brego");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(leaders, heirs, garrison);
		scn.MoveCardsToHand(brego);

		scn.StartGame();
		// Three conditions with one [gondor] token each, alike but that one of them is hindered.
		scn.AddTokensToCard(leaders, 1);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(garrison, 1);
		scn.HinderCard(leaders);

		// Aragorn is the only [gondor] companion, and bears Brego unasked.
		scn.FreepsPlayCard(brego);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brego));
		scn.FreepsAcceptOptionalTrigger();

		// The token on the hindered condition is not one that may be reinforced.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(heirs, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders));
		scn.FreepsChooseCard(heirs);
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(leaders);
		assertEquals(1, scn.GetCultureTokensOn(leaders));
	}

	@Test
	public void NoCultureTokenIsAddedToAHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		var garrison = scn.GetFreepsCard("garrison");
		var knowledge1 = scn.GetFreepsCard("knowledge1");
		var knowledge2 = scn.GetFreepsCard("knowledge2");
		// Four companions: room for four threats.
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveCardsToSupportArea(leaders, heirs, garrison);
		scn.MoveCardsToHand(knowledge1, knowledge2);

		scn.StartGame();
		scn.AddTokensToCard(leaders, 1);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(garrison, 1);
		scn.HinderCard(leaders);
		// The top five cards of the Shadow player's draw deck: two Free Peoples cards and three Shadow cards.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("sam"), scn.GetShadowCard("gimli"),
				scn.GetShadowCard("whelp"), scn.GetShadowCard("uruk"), scn.GetShadowCard("champion"));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(knowledge1);
		scn.DismissRevealedCards();
		assertEquals(3, scn.GetThreats());

		// A token for each of the two Free Peoples cards.  Each may go on either of the two conditions that
		// are not hindered, and neither on the hindered one, though it has a [gondor] token on it.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(heirs, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders));
		scn.FreepsChooseCard(heirs);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders));
		scn.FreepsChooseCard(garrison);
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With every condition that has a token hindered, the event is still played, and still adds its
		// threat; the tokens for the four Free Peoples cards revealed go nowhere.
		scn.HinderCard(heirs, garrison);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("sam"), scn.GetShadowCard("gimli"),
				scn.GetShadowCard("elrond"), scn.GetShadowCard("ranger"), scn.GetShadowCard("whelp"));
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(knowledge2);
		scn.DismissRevealedCards();
		assertEquals(4, scn.GetThreats());
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(leaders, heirs, garrison);
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
	}

	@Test
	public void CultureTokensOnAHinderedCardCannotPayACost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var brego = scn.GetFreepsCard("brego");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brego);
		scn.MoveCardsToSupportArea(leaders, heirs);

		scn.StartGame();
		// Three [gondor] tokens: two on Noble Leaders and one on Heirs of Gondor.
		scn.AddTokensToCard(leaders, 2);
		scn.AddTokensToCard(heirs, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(brego));

		// With Noble Leaders hindered one token is left that can be removed: one short of the cost, though the
		// bearer can exert and a minion is there to wound.
		scn.HinderCard(leaders);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(brego));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.RestoreCard(leaders);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(brego));

		// With Heirs of Gondor hindered in its place two are left, both on Noble Leaders, and they pay.  The
		// token on the hindered card stays where it is.
		scn.HinderCard(heirs);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(brego));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsUseCardAction(brego);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
