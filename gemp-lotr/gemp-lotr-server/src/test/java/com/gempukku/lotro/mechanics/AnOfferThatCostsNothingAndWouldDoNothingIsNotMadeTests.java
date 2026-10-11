package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * An optional trigger, or an ability of a card in play, that costs nothing and whose whole effect would do nothing
 * is not offered.
 *
 * Ketura's ruling on Q-0034 (2026-10-06): an optional action with NO COST that would do nothing is not offered;
 * an event, and anything with a cost, is offered whatever its effect will do.  On Q-0224 (2026-10-10): the game
 * withholds such an offer itself, from what the effects say of themselves, "to 80/20 the work so that it's
 * majority not work held only in the card definitions"; and on Q-0231 the same day: an offer that ANOTHER card's
 * bar makes empty is not made either.
 *
 * The rule is IdleOffers.  Most effects could already say that they are certain to do nothing (nobody to heal,
 * no threat that can be added, no site to take): that is the forecast the no-effect flag and the preventions are
 * judged from.  Some kinds of effect were taught one thing more, for this rule alone.  Each has its tests
 * here, both ways: the offer made on a board where the effect can do something, and not made where it cannot.
 * <ul>
 * <li>reinforcing a token: no card bears a token of that culture;</li>
 * <li>a transfer: no card to transfer, nobody for it to go to, or a card that is to go "from your support
 * area" and has left it;</li>
 * <li>an assignment to a skirmish: nobody the minion could be assigned to;</li>
 * <li>making an ally take part in skirmishes: no ally;</li>
 * <li>stacking a card from play, and from a discard pile: nothing to stack, or nothing to stack it on;</li>
 * <li>a restore: no card that can be restored;</li>
 * <li>putting on The One Ring: it is worn already;</li>
 * <li>a discard from the top of a draw deck: the deck is empty, or the card may not discard from it;</li>
 * <li>a card in play chosen for a later effect, and a card chosen from a discard pile: nothing to choose;</li>
 * <li>an effect that is repeated: what is repeated would do nothing the first time;</li>
 * <li>a number that is named: naming it changes nothing, so it does not keep the offer by itself;</li>
 * <li>a discard at random from a hand: the hand is empty, or the card may not discard from it;</li>
 * <li>twilight removed as an effect: the pool is empty;</li>
 * <li>a card stacked from hand, a chosen discard from hand, a card placed from a hand on a draw deck: no card in
 * that hand to take (of another player's hand, only that it is empty);</li>
 * <li>a card placed from a discard pile beneath a draw deck: no such card in the pile.</li>
 * </ul>
 *
 * Four of the kinds are shown with the tests' own cards (Test Muster, Test Salve, Test Almanac, Test Refrain):
 * every real card that has one of them in an offer that costs nothing carries a requirement written by hand
 * before the rule was built, which answers first.  Those requirements stay.
 *
 * What the rule leaves alone is here too: an ability with a cost is offered though its effect will do nothing
 * (it is flagged), and an optional draw is offered with the draw deck empty (Ketura on Q-0029: "let it ask with
 * 0 cards in deck").  The rule does not reach a "you may" inside another action (Q-0224: A2).
 */
public class AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests
{
	// Bill the Pony, Dearly-loved: "Bearer must be a Ring-bound Hobbit.  When you play Bill the Pony, you may
	// reinforce a [shire] token."  Its Regroup ability removes 2 [shire] tokens, and no test here reaches it.
	protected VirtualTableScenario GetReinforceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pony", "13_143");
					// Sudden Fury: [shire] support-area condition that bears [shire] tokens (one is added each time a
					// [shire] companion loses a skirmish; its Skirmish ability removes one)
					put("fury", "12_132");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Too Late: [wraith] support-area condition.  "Skirmish: Transfer this condition from your support area to a
	// character skirmishing a Nazgul."
	protected VirtualTableScenario GetTransferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("late", "7_209");
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a Nazgul, strength 9; her text needs a [wraith] condition on a companion
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: a second Nazgul, strength 10; her text needs 2 burdens or 2 wounds on the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, no Nazgul
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Ulaire Toldea, Messenger of Morgul: "Fierce.  Assignment: Spot 4 burdens to assign a companion (except the
	// Ring-bearer) to skirmish Ulaire Toldea."  A spot is no cost.
	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "1_236");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The tests' own cards, each an ability with no cost and no requirement:
	// Test Muster, "Maneuver: Make an ally participate in skirmishes until the regroup phase."
	// Test Salve, "Fellowship: Restore a Free Peoples card."
	// Test Almanac, "Fellowship: Choose a Free Peoples card in your discard pile and take it into hand."
	// Test Refrain, "Fellowship: Heal a companion, and again for as long as a companion was healed."
	protected VirtualTableScenario GetTestCardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("muster", "0_8029");
					put("salve", "0_8030");
					put("almanac", "0_8031");
					put("refrain", "0_8032");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a Maneuver ability used only on purpose
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, home site 3; at site 2 he takes no part in skirmishes
					put("runner", "1_178");     // Goblin Runner: a minion, so that the maneuver phase is played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Dunlending Looter: [dunland] Man, strength 9, vitality 1.  "When this minion wins a skirmish, you may stack
	// him on a site you control."
	protected VirtualTableScenario GetStackFromPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("looter", "4_11");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, so the Looter wins by one
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Vengeful Savage: [men] Man, strength 10, vitality 2.  "Each time this minion wins a skirmish, you may stack a
	// [men] minion from your discard pile on a [men] possession in your support area."
	protected VirtualTableScenario GetStackFromDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "17_62");
					// Fleet of Corsair Ships: [men] support-area possession; its offer of a token comes only when it
					// is played, and its Regroup ability costs 4 tokens
					put("fleet", "18_66");
					put("corps", "11_73");      // Corps of Harad: a [men] minion, for the discard pile
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, so the Savage wins
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Narchost, site 9 of the King block: "Shadow: Spot 8 burdens to make the Ring-bearer wear The One Ring until
	// the regroup phase."  The other sites are the usual King-block ones; the Ring-bearer is Frodo 10_121,
	// resistance 10.
	protected VirtualTableScenario GetRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				new HashMap<>() {{
					putAll(VirtualTableScenario.KingSites);
					put("site9", "7_362");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Soldier: "Damage +1.  When you play this minion, you may make the Free Peoples player discard the top
	// card of his draw deck."  The Free Peoples player's own two copies are his whole draw deck.
	protected VirtualTableScenario GetDiscardFromDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier1", "1_154");
					put("soldier2", "1_154");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gwaihir, The Windlord: [gandalf] follower, strength +1.  "If bearer is a [gandalf] Wizard, each time bearer
	// wins a skirmish, you may exert a minion (if that minion is a Wizard, wound it instead)."  The minion is
	// chosen first and what is done to it follows.
	protected VirtualTableScenario GetChosenCardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwaihir", "17_20");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength 6, +1 for the Hobbit in the fellowship
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1: the wound of a lost skirmish kills it
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3: it loses, lives and can exert
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Ever My Heart Rises: besides its printed Fellowship ability it offers "Discard cards from the top of your
	// deck to no effect" (spot a Dwarf; a number of times is named, and five cards are discarded for each: Ketura
	// on Q-0124).  Two Axe Strikes are the Free Peoples draw deck.
	protected VirtualTableScenario GetNumberScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heart", "4_46");
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf to spot
					put("axe1", "1_3");         // Axe Strike: a skirmish event, a card in the draw deck and nothing more
					put("axe2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Orc Inquisitor: "When you play this minion, you may make the Free Peoples player discard a card at random
	// from hand."  Frodo, Courteous Halfling is the Ring-bearer: "While you can spot 3 unbound companions, Shadow
	// cards may not discard cards from your hand or from the top of your draw deck."
	protected VirtualTableScenario GetRandomDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inquisitor1", "1_268");
					put("inquisitor2", "1_268");
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: an unbound companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: the third unbound companion
					put("sam", "1_311");        // Sam, Son of Hamfast: the card in hand
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	// Sleeping Quarters, site 1 of the King block: "When the fellowship moves from this site, the Free Peoples
	// player may spot 3 Hobbits to remove (2)."  The other sites are the usual King-block ones.
	protected VirtualTableScenario GetTwilightScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit; a Fellowship ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit; stronger only while he bears a weapon
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.KingSites);
					put("site1", "7_333");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Shards of Narsil: [gondor] artifact in the support area.  "Fellowship: Stack a [gondor] card from hand
	// here."  Its other Fellowship ability, "Add (1) to take a card stacked here into hand", has a cost.
	protected VirtualTableScenario GetStackFromHandScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shards", "3_44");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] card
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Longbottom Leaf: [shire] possession, cost 1.  "Pipeweed.  Plays to your support area.  When you play this
	// possession, you may discard up to 2 cards from hand."
	protected VirtualTableScenario GetDiscardFromHandScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf1", "1_300");
					put("leaf2", "1_300");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a card in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Orkish Warg-master: unique [orc] Orc, strength 8, vitality 2.  "Each time a mounted [orc] minion wins a
	// skirmish, you may make the Free Peoples player place a card from his or her hand on top of his or her draw
	// deck."  His Shadow ability exerts him, and no test here reaches it.
	protected VirtualTableScenario GetPlaceFromHandScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("master", "17_87");
					// Threatening Warg: [orc] mount, strength +2, vitality +1, on an [orc] Orc of strength 10 or less;
					// "Bearer is fierce."  Its own offer comes only when it is played.
					put("warg", "17_91");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4: he loses to 10 twice and lives
					put("gimli", "1_13");       // Gimli, Son of Gloin: the card in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Goblin Sneak: [moria] Orc, cost 1.  "When you play this minion, you may place a [moria] Orc from your
	// discard pile beneath your draw deck."
	protected VirtualTableScenario GetPlaceFromDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak1", "1_181");
					put("sneak2", "1_181");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, for the discard pile
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, for the discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Soldier again, with Frodo, Courteous Halfling as the Ring-bearer: "While you can spot 3 unbound
	// companions, Shadow cards may not discard cards from your hand or from the top of your draw deck."
	protected VirtualTableScenario GetBarredDiscardFromDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier1", "1_154");
					put("soldier2", "1_154");
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: an unbound companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: the third unbound companion
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Shaman: "Damage +1.  Maneuver: Remove (2) to heal an Uruk-hai."
	protected VirtualTableScenario GetCostScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shaman", "1_152");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Old Toby: [shire] possession, cost 1.  "Pipeweed.  Plays to your support area.  When you play this
	// possession, you may draw a card."  The Free Peoples player's draw deck is empty once it is in his hand.
	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReinforcingATokenIsOfferedWhereACardBearsOneAndNotWhereNoneDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReinforceScenario();

		var pony = scn.GetFreepsCard("pony");
		var fury = scn.GetFreepsCard("fury");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCardsToHand(pony);

		scn.StartGame();
		scn.AddTokensToCard(fury, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetCultureTokensOn(fury));

		scn.FreepsPlayCard(pony);
		assertAttachedTo(pony, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pony));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(fury));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReinforcingATokenIsNotOfferedWhereNoCardBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReinforceScenario();

		var pony = scn.GetFreepsCard("pony");
		var fury = scn.GetFreepsCard("fury");
		var frodo = scn.GetRingBearer();
		// A [shire] card that could bear a token, and bears none.
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCardsToHand(pony);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(fury));

		scn.FreepsPlayCard(pony);
		assertAttachedTo(pony, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pony));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ATransferIsOfferedWhereTheCardHasSomebodyToGoToAndNotWhereItHasNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var late = scn.GetShadowCard("late");
		var lemenya = scn.GetShadowCard("lemenya");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(late);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, lemenya});

		// Aragorn's skirmish is against an Uruk-hai: nobody is skirmishing a Nazgul, and Too Late has nowhere to go.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(late));
		scn.ShadowPass();

		// Gimli's is against Ulaire Lemenya.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(late));

		scn.ShadowUseCardAction(late);
		assertAttachedTo(late, gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAssignmentIsOfferedWhereSomebodyCanBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(toldea);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(toldea));

		scn.ShadowUseCardAction(toldea);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAssignmentIsNotOfferedWhereNobodyCanBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var frodo = scn.GetRingBearer();
		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(toldea);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The burdens are there to spot, and the Ring-bearer is the only companion: "(except the Ring-bearer)".
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(toldea));
		assertFalse(scn.IsCharAssigned(frodo));
	}

	@Test
	public void MakingAnAllyTakePartInSkirmishesIsOfferedWhereThereIsAnAllyAndNotWhereThereIsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTestCardScenario();

		var muster = scn.GetFreepsCard("muster");
		var elrond = scn.GetFreepsCard("elrond");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(muster);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// No ally is in play.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(muster));

		scn.MoveAlliesToTable(elrond);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(muster));
		assertFalse(scn.FreepsCanAssign(elrond));

		scn.FreepsUseCardAction(muster);
		assertTrue(scn.FreepsCanAssign(elrond));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ARestoreIsOfferedWhereACardCanBeRestoredAndNotWhereNoneCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTestCardScenario();

		var salve = scn.GetFreepsCard("salve");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(salve);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// No card is hindered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(salve));

		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(salve));

		scn.FreepsUseCardAction(salve);
		assertFalse(scn.IsHindered(aragorn));
		// Nothing is hindered again, and the offer has gone.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(salve));
	}

	@Test
	public void ACardChosenFromADiscardPileIsOfferedWhereThereIsOneToChooseAndNotWhereThereIsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTestCardScenario();

		var almanac = scn.GetFreepsCard("almanac");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(almanac);

		scn.StartGame();

		// The discard pile is empty.
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(almanac));

		scn.MoveCardsToDiscard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(almanac));

		scn.FreepsUseCardAction(almanac);
		assertInHand(aragorn);
		// The pile is empty again, and the offer has gone.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(almanac));
	}

	@Test
	public void ARepeatedEffectIsOfferedWhereItWouldDoSomethingTheFirstTimeAndNotWhereItWouldNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTestCardScenario();

		var refrain = scn.GetFreepsCard("refrain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(refrain);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// No companion is wounded.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(refrain));

		scn.AddWoundsToChar(aragorn, 2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(refrain));

		// One wound is healed, and then the other.
		scn.FreepsUseCardAction(refrain);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(refrain));
	}

	@Test
	public void StackingACardFromPlayIsOfferedWhereThereIsSomethingToStackItOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackFromPlayScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var looter = scn.GetShadowCard("looter");

		scn.StartGame();
		scn.SkipToSite(2);
		// Both markers have passed site 1.
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(looter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, looter);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));

		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, looter);
		assertTrue(scn.GetStackedCards(scn.GetSite(1)).contains(looter));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StackingACardFromPlayIsNotOfferedWhereThereIsNothingToStackItOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackFromPlayScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var looter = scn.GetShadowCard("looter");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(looter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, looter);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The Looter has won, and the Shadow player controls no site.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StackingACardFromADiscardPileIsOfferedWhereThereIsACardToStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackFromDiscardScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var savage = scn.GetShadowCard("savage");
		var fleet = scn.GetShadowCard("fleet");
		var corps = scn.GetShadowCard("corps");
		scn.MoveCardsToSupportArea(fleet);
		scn.MoveCardsToDiscard(corps);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));

		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, corps);
		assertTrue(scn.GetStackedCards(fleet).contains(corps));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StackingACardFromADiscardPileIsNotOfferedWhereThereIsNoCardToStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackFromDiscardScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var savage = scn.GetShadowCard("savage");
		var fleet = scn.GetShadowCard("fleet");
		// The possession to stack on is there; the Shadow player's discard pile is empty.
		scn.MoveCardsToSupportArea(fleet);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowDiscardCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PuttingOnTheOneRingIsOfferedWhileItIsNotWornAndNotOnceItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(8);
		// The fellowship moves to site 9.
		scn.FreepsPass();

		var narchost = scn.GetCurrentSite();
		assertEquals("Narchost", narchost.getBlueprint().getTitle());
		assertEquals(8, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(narchost));

		scn.ShadowUseCardAction(narchost);
		assertTrue(scn.RBWearingOneRing());
		// The burdens are still there to spot; the Ring cannot be put on a second time.
		assertEquals(8, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(narchost));
	}

	@Test
	public void ADiscardFromTheTopOfADrawDeckIsOfferedWhileThatDeckHasACardAndNotOnceItIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardFromDeckScenario();

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(soldier1, soldier2);
		// One of the Free Peoples player's two cards is taken out of his draw deck.
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("soldier2"));

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(soldier1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier1));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(soldier2);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardChosenForALaterEffectIsOfferedWhereThereIsOneToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChosenCardScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// The Uruk Savage lost, took a wound, and can still exert.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardChosenForALaterEffectIsNotOfferedWhereThereIsNoneToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChosenCardScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// The Goblin Runner lost and was killed by its wound: no minion is left to choose.
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ANumberThatIsNamedDoesNotKeepTheOfferWhenWhatFollowsWouldDoNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNumberScenario();

		var heart = scn.GetFreepsCard("heart");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(heart);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// Two cards in the draw deck: the discard can do something, and the ability is offered.
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(heart, "to no effect"));

		scn.FreepsUseCardAction(heart, "to no effect");
		scn.FreepsChoose("1");
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());

		// The draw deck is empty: whatever number is named nothing will be discarded, and it is not offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(heart, "to no effect"));
	}

	@Test
	public void ADiscardAtRandomFromHandIsOfferedWhereTheCardMayDiscardFromThatHandAndNotWhereItMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRandomDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var inquisitor2 = scn.GetShadowCard("inquisitor2");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(sam, inquisitor1, inquisitor2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Two unbound companions: Frodo's bar is not in force, and the Inquisitor may discard the card in hand.
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(inquisitor1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor1));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(sam);

		// A third: "Shadow cards may not discard cards from your hand".  The card is still there to discard, and
		// the second Inquisitor's offer is not made.
		scn.MoveCompanionsToTable(gimli);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(inquisitor2);
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor2));
		assertInHand(sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ATransferFromASupportAreaIsNotOfferedOnceTheCardHasLeftIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var late = scn.GetShadowCard("late");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToSupportArea(late);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, nelya});

		// Aragorn's skirmish is against a Nazgul, and Too Late goes to him from the support area.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertInZone(Zone.SUPPORT, late);
		assertTrue(scn.ShadowActionAvailable(late));
		scn.ShadowUseCardAction(late);
		assertAttachedTo(late, aragorn);
		scn.PassSkirmishActions();

		// Gimli's is against a Nazgul too: there is somebody for the condition to go to, and it is no longer in
		// the support area it is to be transferred from.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(late));
		assertAttachedTo(late, aragorn);
	}

	@Test
	public void TwilightRemovedAsAnEffectIsOfferedWhileThePoolHoldsAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwilightScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		var quarters = scn.GetCurrentSite();
		assertEquals("Sleeping Quarters", quarters.getBlueprint().getTitle());
		scn.SetTwilight(2);

		// The fellowship moves from Sleeping Quarters with three Hobbits and (2) in the pool.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(quarters));
		assertEquals(2, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		// The (2) are gone; what is in the pool is what the move added: 3 for the companions and 1, the Shadow
		// number of King's Tent.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwilightRemovedAsAnEffectIsNotOfferedWhereThePoolIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwilightScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		var quarters = scn.GetCurrentSite();
		assertEquals("Sleeping Quarters", quarters.getBlueprint().getTitle());

		// The three Hobbits are there to spot, and the pool is empty.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(quarters));
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 for the companions and 1, the Shadow number of King's Tent.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StackingACardFromHandIsOfferedWhereTheHandHoldsSuchACardAndNotWhereItHoldsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackFromHandScenario();

		var shards = scn.GetFreepsCard("shards");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(shards);
		// A card in hand that is not a [gondor] card.
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shards, "Stack"));

		scn.MoveCardsToHand(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(shards, "Stack"));

		scn.FreepsUseCardAction(shards, "Stack");
		assertInZone(Zone.STACKED, aragorn);
		assertTrue(scn.GetStackedCards(shards).contains(aragorn));
		assertInHand(gimli);
		// No [gondor] card is left in hand, and the offer has gone.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shards, "Stack"));
	}

	@Test
	public void ADiscardFromHandIsOfferedWhileTheHandHoldsACardAndNotOnceItIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardFromHandScenario();

		var leaf1 = scn.GetFreepsCard("leaf1");
		var leaf2 = scn.GetFreepsCard("leaf2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(leaf1, leaf2, gimli);

		scn.StartGame();

		scn.FreepsPlayCard(leaf1);
		assertInZone(Zone.SUPPORT, leaf1);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(leaf1));

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);
		assertInDiscard(gimli);
		assertEquals(1, scn.GetFreepsHandCount());

		// The second Leaf is the last card in hand: once it is played there is nothing to discard.
		scn.FreepsPlayCard(leaf2);
		assertInZone(Zone.SUPPORT, leaf2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(leaf2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardPlacedFromAHandOnADrawDeckIsOfferedWhileThatHandHoldsACardAndNotOnceItIsEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlaceFromHandScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli);
		var master = scn.GetShadowCard("master");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.MoveMinionsToTable(master);
		scn.AttachCardsTo(master, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, master);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The mounted Warg-master (8 + 2) has beaten Aragorn (8), and the Free Peoples player holds one card.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertSame(gimli, scn.GetFreepsTopOfDeck());

		// The Warg makes its bearer fierce: he fights Aragorn again, wins again, and that hand is now empty.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, master);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(master));
		assertSame(gimli, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACardPlacedFromADiscardPileBeneathADrawDeckIsOfferedWhereThePileHoldsSuchACardAndNotWhereItHoldsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlaceFromDiscardScenario();

		var sneak1 = scn.GetShadowCard("sneak1");
		var sneak2 = scn.GetShadowCard("sneak2");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(sneak1, sneak2);
		scn.MoveCardsToDiscard(runner, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [moria] Orc and an Uruk-hai are in the Shadow player's discard pile.
		scn.ShadowPlayCard(sneak1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak1));
		scn.ShadowAcceptOptionalTrigger();
		assertSame(runner, scn.GetShadowBottomOfDeck());
		assertInDiscard(savage);

		// Only the Uruk-hai is left there: no [moria] Orc to place.
		scn.ShadowPlayCard(sneak2);
		assertInZone(Zone.SHADOW_CHARACTERS, sneak2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sneak2));
		assertInDiscard(savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADiscardFromTheTopOfADrawDeckIsOfferedWhereTheCardMayDiscardFromThatDeckAndNotWhereItMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarredDiscardFromDeckScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(soldier1, soldier2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Two unbound companions: Frodo's bar is not in force, and the Soldier may discard from the deck.
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 0);
		scn.ShadowPlayCard(soldier1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier1));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(deck, scn.GetFreepsDeckCount());

		// A third: "Shadow cards may not discard cards ... from the top of your draw deck".  The deck still has
		// its cards, and the second Soldier's offer is not made.
		scn.MoveCompanionsToTable(gimli);
		scn.ShadowRefreshDecision();
		deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 0);
		scn.ShadowPlayCard(soldier2);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAbilityWithACostIsOfferedThoughItsEffectWillDoNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCostScenario();

		var shaman = scn.GetShadowCard("shaman");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// No Uruk-hai is wounded: there is nobody to heal.  The ability has a price, so it is offered, and flagged.
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(shaman));
		assertEquals("effect", scn.ShadowActionNoEffect(shaman));

		scn.ShadowUseCardAction(shaman);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnOptionalDrawIsStillOfferedWithTheDrawDeckEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(toby);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(toby);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
