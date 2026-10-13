package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Culture tokens: placed on a card, spotted, reinforced, removed, and lost when the card leaves play.
 *
 * Comprehensive Rules 4.2, "culture tokens": "Some conditions tell you to 'place an [Elven] token on this card'
 * (or 'a [Raider] token,' and so on)."
 *
 * "Reinforce": "This verb, used in game text, means to add a culture token to a card that already has a token of
 * the same culture.  You may choose any of your cards with the appropriate culture token if you have more than
 * one.  If an effect lets you reinforce more than one culture token, you may put them all on one card or divide
 * them up however you choose."  Its example: "Your card says, 'When you play this possession, you may reinforce a
 * [Dwarven] culture token.'  When you play this card, you may choose any of your Dwarven cards that currently has
 * one or more [Dwarven] culture tokens and add another token to that card."  (Arod, Rohirrim Steed.)
 *
 * Whose cards: the first sentence names no owner and the second says "any of your cards".  Ketura, 2026-10-11
 * (F-0468): the two disagree and the rules council is being asked; until it answers, either player may choose
 * either player's card ("there are some cards whose tokens 'count up' and then self-discard upon accumulation ...
 * which is why someone might want the option to reinforce an opponent's card").  Morannon Plains 18_137 is where
 * that shows; a different answer from the council turns its test round.
 *
 * "Leaving play": "When a card (other than a site) leaves play for any reason ...  Any tokens on that card are
 * removed."  "Active": "Inactive cards are not affected by the game and do not affect the game.  Any tokens on
 * them are ignored (such as burdens, culture tokens, threats, and wounds)."
 *
 * Ketura, 2026-10-08, on "remove 2 [raider] tokens": the tokens of one cost may be taken "from multiple targets".
 * And on F-0113: several tokens reinforced are chosen for one at a time.
 *
 * A token is laid by the rig only on a card that a card of the game gives tokens to, and of the number it gives.
 * The tokens on a hindered card are TokensOnAHinderedCardTests; that a reinforcement with no cost is not offered
 * with no token to reinforce is the engine's rule for every such offer
 * (AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests) and is read once here on the rules' own card.
 */
public class CultureTokensTests
{
	protected VirtualTableScenario GetScenario(HashMap<String, String> sites, String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: strength 6, vitality 3, damage +1
					put("gimli", "1_13");
					// Ranger of the South: strength 5.  "While you can spot a [gondor] token, this companion is
					// strength +2."
					put("ranger", "18_56");

					// A Path Appointed: "Fellowship: Exert a [gondor] Man and either a [dwarven] companion or a
					// [rohan] companion to add a [gondor] token here.  Regroup: Remove a [gondor] token here to
					// discard a minion."
					put("path", "8_42");
					// Garrison of Osgiliath: "When you play this condition, you may spot a [gondor] Man to place 2
					// [gondor] tokens here."  Its skirmish ability is not used.
					put("garrison", "6_52");
					// Heirs of Gondor: "When you play this, add a [gondor] token here."  Its skirmish ability is
					// not used.
					put("heirs", "13_69");
					// Noble Leaders: its tokens come as it is played, one for each of four named Men
					put("leaders", "7_112");
					// Guarded City: cost 1.  "Spot a [gondor] companion to reinforce a [gondor] token for each
					// possession borne by that companion."
					put("city1", "13_68");
					put("city2", "13_68");
					put("sword", "1_112");
					// Ranger's Cloak: on a [gondor] Man; vitality +1 and resistance +1
					put("cloak", "15_68");

					// Arod, Rohirrim Steed: "Bearer must be a Dwarf.  When you play Arod, you may reinforce a
					// [dwarven] token.  Response: If bearer is about to take a wound during a skirmish, remove 2
					// [dwarven] tokens to prevent that."
					put("arod", "13_1");
					// Suspended Palaces: "When you play this condition, you may spot a Dwarf to place 2 [dwarven]
					// tokens here.  Fellowship: Discard this condition or remove a [dwarven] token from here to
					// stack the top card of your draw deck on a [dwarven] condition that has a card stacked on it."
					put("palaces", "6_10");
					// Stout and Strong: "Each time a Dwarf wins a skirmish, you may place a [dwarven] token on this
					// card."
					put("stout", "4_57");
					// My Axe Is Notched: a [dwarven] token each time Gimli wins a skirmish
					put("axe", "4_52");

					// Ships of Great Draught: [raider] possession in the support area, not unique.  "Shadow: Remove
					// a threat or discard a [raider] card from hand to add a [raider] token here."
					put("ships1", "8_65");
					put("ships2", "8_65");
					// Orc Soldier: strength 7, vitality 2; its skirmish ability is not used
					put("soldier", "1_271");
					// Goblin Patrol Troop: no text
					put("troop", "1_177");
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FellowshipSites, VirtualTableScenario.Multipath);
	}

	// A Shadows-block path.  Morannon Plains: "Battleground.  Plains.  When the fellowship moves to this site,
	// each player may reinforce a culture token."  The Angle, laid before it, is a forest whose Shadow action
	// needs a Nazgûl.
	protected VirtualTableScenario GetMorannonScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site2", "18_137");
				}},
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void ATokenPlacedOnACardIsSpottedAndStaysThereFromTurnToTurnUntilItIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var ranger = scn.GetFreepsCard("ranger");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn, gimli, ranger);
		scn.MoveCardsToSupportArea(path);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(5, scn.GetStrength(ranger));

		// "Add a [gondor] token here": Aragorn or the Ranger exerts, and Gimli.
		scn.FreepsUseCardAction(path);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(7, scn.GetStrength(ranger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn passes, and the token is there at the start of the next.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(7, scn.GetStrength(ranger));

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(7, scn.GetStrength(ranger));

		// "Remove a [gondor] token here to discard a minion"
		scn.FreepsUseCardAction(path);
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(5, scn.GetStrength(ranger));
		assertInZone(Zone.DISCARD, troop);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReinforcingAddsATokenToACardThatAlreadyHasOneOfThatCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arod = scn.GetFreepsCard("arod");
		var palaces = scn.GetFreepsCard("palaces");
		var stout = scn.GetFreepsCard("stout");
		var axe = scn.GetFreepsCard("axe");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(palaces, stout, axe, garrison);
		scn.MoveCardsToHand(arod);

		scn.StartGame();
		// Two [dwarven] cards with [dwarven] tokens, one with none, and a [gondor] card with [gondor] tokens
		scn.AddTokensToCard(palaces, 2);
		scn.AddTokensToCard(stout, 1);
		scn.AddTokensToCard(garrison, 2);

		// The rules' example: "When you play this possession, you may reinforce a [Dwarven] culture token."
		scn.FreepsPlayCard(arod);
		assertAttachedTo(arod, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arod));
		scn.FreepsAcceptOptionalTrigger();

		// "Any of your Dwarven cards that currently has one or more [Dwarven] culture tokens"
		assertTrue(scn.FreepsDecisionAvailable("Choose card to reinforce"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(palaces, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, garrison));

		// "... and add another token to that card."
		scn.FreepsChooseCard(stout);
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetCultureTokensOn(axe));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTokenOfThatCultureOnAnyCardNothingIsReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arod = scn.GetFreepsCard("arod");
		var axe = scn.GetFreepsCard("axe");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(axe, garrison);
		scn.MoveCardsToHand(arod);

		scn.StartGame();
		// A [dwarven] card with no token, and tokens of another culture
		scn.AddTokensToCard(garrison, 2);

		scn.FreepsPlayCard(arod);
		assertAttachedTo(arod, gimli);

		// A card with no token is not given its first by a reinforcement, and a [gondor] token is no
		// [dwarven] token: nothing is offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(0, scn.GetCultureTokensOn(axe));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SeveralTokensReinforcedAreChosenForOneAtATimeAndMayBeDividedOrAllGoOnOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var cloak = scn.GetFreepsCard("cloak");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var leaders = scn.GetFreepsCard("leaders");
		var city1 = scn.GetFreepsCard("city1");
		var city2 = scn.GetFreepsCard("city2");
		scn.MoveCompanionsToTable(aragorn);
		// Two possessions on the one [gondor] companion: two tokens to reinforce
		scn.AttachCardsTo(aragorn, sword, cloak);
		scn.MoveCardsToSupportArea(garrison, heirs, leaders);
		scn.MoveCardsToHand(city1, city2);

		scn.StartGame();
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);

		// Divided: one on the Garrison and one on the Heirs.  Noble Leaders has no token and is never offered.
		scn.FreepsPlayCard(city1);
		assertTrue(scn.FreepsDecisionAvailable("Choose card to reinforce"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders));
		scn.FreepsChooseCard(garrison);
		assertEquals(3, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		assertTrue(scn.FreepsDecisionAvailable("Choose card to reinforce"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(heirs);
		assertEquals(3, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// All on one card: both on the Garrison
		scn.FreepsPlayCard(city2);
		scn.FreepsChooseCard(garrison);
		assertEquals(4, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.FreepsDecisionAvailable("Choose card to reinforce"));
		scn.FreepsChooseCard(garrison);
		assertEquals(5, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtMorannonPlainsEachPlayerMayReinforceATokenOnACardOfEitherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMorannonScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(garrison, heirs, leaders);

		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var angle = scn.GetShadowSite("site8");
		var morannon = scn.GetShadowSite("site2");

		scn.StartGame(scn.GetFreepsSite("site1"));
		// Two of the Free Peoples player's cards with [gondor] tokens, and Noble Leaders with none
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);

		// To The Angle, where the Shadow player has two cards of his own with [raider] tokens in play ...
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(angle);
		scn.MoveCardsToSupportArea(ships1, ships2);
		scn.AddTokensToCard(ships1, 1);
		scn.AddTokensToCard(ships2, 1);

		// ... and on to Morannon Plains: "each player may reinforce a culture token", of whatever culture.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(morannon);

		// The Free Peoples player is offered every card that has a culture token, the Shadow player's Ships
		// beside his own two, and puts his token on one of the Ships.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to reinforce a culture token"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsDecisionAvailable("Choose card to reinforce"));
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs, ships1, ships2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders));
		scn.FreepsChooseCard(ships1);
		assertEquals(2, scn.GetCultureTokensOn(ships1));

		// The Shadow player is offered the same four, and puts his on the Free Peoples player's Heirs of Gondor.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to reinforce a culture token"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowDecisionAvailable("Choose card to reinforce"));
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(garrison, heirs, ships1, ships2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(leaders));
		scn.ShadowChooseCard(heirs);

		assertEquals(2, scn.GetCultureTokensOn(ships1));
		assertEquals(1, scn.GetCultureTokensOn(ships2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTokensOnACardThatLeavesPlayAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(palaces);

		scn.StartGame();

		// "You may spot a Dwarf to place 2 [dwarven] tokens here"
		scn.FreepsPlayCard(palaces);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palaces));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.SUPPORT, palaces);
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		// The condition discards itself, as the first way to pay for its own ability.
		scn.FreepsUseCardAction(palaces);
		scn.FreepsChoose("Discard this condition");
		assertInZone(Zone.DISCARD, palaces);
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Taken back into hand, as a card that fetches it would take it, and played again with its own two
		// tokens declined: it has none of the tokens it had.
		scn.MoveCardsToHand(palaces);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(palaces);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palaces));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, palaces);
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTokensOnACardThatIsNotActiveAreIgnored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var firstPlayersRanger = scn.GetFreepsCard("ranger");
		var firstPlayersGarrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn, firstPlayersRanger);
		scn.MoveCardsToSupportArea(firstPlayersGarrison);

		var secondPlayersRanger = scn.GetShadowCard("ranger");
		var secondPlayersHeirs = scn.GetShadowCard("heirs");

		scn.StartGame();
		scn.AddTokensToCard(firstPlayersGarrison, 2);

		// On his own turn the first player's tokens are spotted by his own Ranger.
		assertEquals(7, scn.GetStrength(firstPlayersRanger));

		// The second player's turn.  "During your turn, only these cards in play are active: ... your Free
		// Peoples cards": the first player's Garrison is not, and the two tokens still on it are ignored.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCompanionsToTable(secondPlayersRanger);
		scn.SetHand(scn.P2(), secondPlayersHeirs);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, firstPlayersGarrison);
		assertEquals(2, scn.GetCultureTokensOn(firstPlayersGarrison));
		assertEquals(5, scn.GetStrength(secondPlayersRanger));

		// A token on a card of his own is spotted.
		scn.ShadowPlayCard(secondPlayersHeirs);
		assertEquals(1, scn.GetCultureTokensOn(secondPlayersHeirs));
		assertEquals(7, scn.GetStrength(secondPlayersRanger));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TokensRemovedAsOneCostMayBeTakenFromMoreThanOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arod = scn.GetFreepsCard("arod");
		var palaces = scn.GetFreepsCard("palaces");
		var stout = scn.GetFreepsCard("stout");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, arod);
		scn.MoveCardsToSupportArea(palaces, stout, axe);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		// One [dwarven] token on each of two cards, and no card with two
		scn.AddTokensToCard(palaces, 1);
		scn.AddTokensToCard(stout, 1);
		scn.MoveMinionsToTable(soldier);

		// Gimli, 6, loses to the Orc Soldier, 7, and is about to take the wound.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, soldier);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// "Remove 2 [dwarven] tokens to prevent that": one from each card.  The first is chosen for, and the
		// second can only come from the other card.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arod));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("Choose card to remove a token from"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(palaces, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe));
		scn.FreepsChooseCard(palaces);

		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
