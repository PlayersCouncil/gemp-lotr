package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_013_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1's
	// maneuver, skirmish and regroup phases are at The Troll Hoard (site 2), whose only text is a
	// start-of-fellowship offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beorn", "31_13");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion; a discard bar that needs 2 [dwarven]
					// followers, a discount on Thorin and a fellowship ability
					put("gandalf", "30_26");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; strength +2 while 3 Ents are spotted
					put("avenger", "6_27");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					// Dwalin, Brother of Balin: [dwarven] Dwarf companion, 7/3, damage +1; a maneuver ability, not used
					put("dwalin", "30_9");
					// Librarian, Keeper of Ancient Texts: [gandalf] Man companion; "Regroup: Discard a follower from
					// play to choose one: draw a card; or take a [gandalf] skirmish event ..." (his strength bonus
					// wants [gandalf] tokens, none laid)
					put("librarian", "18_22");
					// Dori, Really a decent fellow: [dwarven] follower, Aid exert Bilbo; bearer is hard to overwhelm
					put("dori", "30_8");
					// Bombur, Chief Cook: [dwarven] follower, cost 3, Aid exert a [dwarven] companion; bearer is damage +1
					put("bombur", "30_7");
					// Elf Army: [elven] Elf ally, cost 3, home 5H, archer; a maneuver ability, not used
					put("elfarmy", "31_8");
					// Galadriel, Elven Lady: [elven] Elf ally, cost 3; a start-of-turn heal of Gandalf and a skirmish
					// ability, not used
					put("galadriel", "32_13");
					// Radagast, The Brown: [gandalf] Wizard ally; a fellowship ability, not used
					put("radagast", "31_17");
					// Saruman, The White: [gandalf] Wizard ally, laid in the SHADOW player's discard pile
					put("saruman", "32_24");
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact, hand weapon; bearer must be Gandalf
					put("courage", "30_30");    // He gives me courage: [gandalf] maneuver event
					// Yazneg, Orkish Assassin: [gundabad] Orc; "Maneuver: Remove (3) and discard this minion from play
					// to discard a follower"
					put("yazneg", "30_41");
					// Intimidating Uruk: [uruk-hai] Uruk-hai, 10/3, damage +1; a "when you play" text, not reached
					put("uruk", "11_189");
					// Fury of the Evil Army: [uruk-hai] condition on an [uruk-hai] minion, strength +1; "Each time
					// bearer wins a skirmish, you may choose one: discard a follower from play; heal ...; or control a site"
					put("fury", "18_116");
					// Grond, Hammer of the Underworld: [sauron] possession, support area; "Regroup: Remove X [sauron]
					// tokens here to discard a Free Peoples card (except a companion) with a twilight cost of X.
					// Discard a [sauron] minion or this possession."
					put("grond", "8_103");
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, 5/1; a "when you play" trigger, not reached
					// Uruk Seeker: [isengard] Uruk-hai, 6/2, fierce; strength +2 and damage +1 only against a bearer of
					// a search card (none is laid)
					put("seeker", "4_195");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, 13/3
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BeornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Beorn, Skin-Changer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Follower
		 * Subtype: Man
		 * Strength: 2
		 * Vitality: 1
		 * Aid: Exert Gandalf.
		 * Game Text: <b>Aid</b> – Exert Gandalf.<br>While Beorn is attached, he cannot be discarded by Shadow cards.<br>Each time bearer wins a skirmish, you may take a [gandalf] card from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beorn");

		assertEquals("Beorn", card.getBlueprint().getTitle());
		assertEquals("Skin-Changer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void AidExertsGandalfToTransferBeornToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var balin = scn.GetFreepsCard("balin");

		scn.MoveCompanionsToTable(gandalf, avenger, balin);
		scn.MoveCardsToSupportArea(beorn);
		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.SUPPORT, beorn);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(beorn));
		scn.FreepsAcceptOptionalTrigger();

		// The one Gandalf exerts unasked; Ent Avenger, a [gandalf] companion of another title, does not.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.FreepsChooseCard(balin);

		assertAttachedTo(beorn, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");

		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(beorn);
		// Vitality 4 with 3 wounds: Gandalf cannot exert. Ent Avenger could, and does not pay.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(beorn));
		assertInZone(Zone.SUPPORT, beorn);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
	}

	@Test
	public void AttachedBeornIsNotOfferedToAShadowCharactersDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var dori = scn.GetFreepsCard("dori");
		var bombur = scn.GetFreepsCard("bombur");
		var bilbo = scn.GetRingBearer();
		var yazneg = scn.GetShadowCard("yazneg");

		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(dori);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.AttachCardsTo(bilbo, bombur);
		scn.MoveMinionsToTable(yazneg);
		scn.SetTwilight(10);
		assertAttachedTo(beorn, balin);
		assertAttachedTo(bombur, bilbo);
		assertInZone(Zone.SUPPORT, dori);
		scn.SkipToPhase(Phase.MANEUVER);
		// The Aids of the three followers, declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(yazneg));
		scn.ShadowUseCardAction(yazneg);
		// Followers in the support area and borne are offered; Beorn, attached, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(dori, bombur));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beorn));
		scn.ShadowChooseCard(dori);

		assertInDiscard(dori, yazneg);
		assertAttachedTo(beorn, balin);
		assertAttachedTo(bombur, bilbo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BeornInTheSupportAreaMayBeDiscardedByAShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var dori = scn.GetFreepsCard("dori");
		var yazneg = scn.GetShadowCard("yazneg");

		scn.MoveCardsToSupportArea(beorn, dori);
		scn.StartGame();
		scn.MoveMinionsToTable(yazneg);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, beorn, dori);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();

		scn.ShadowUseCardAction(yazneg);
		assertTrue(scn.ShadowHasCardChoiceAvailable(beorn, dori));
		scn.ShadowChooseCard(beorn);

		assertInDiscard(beorn, yazneg);
		assertInZone(Zone.SUPPORT, dori);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AttachedBeornIsNotOfferedToAShadowConditionsDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var dori = scn.GetFreepsCard("dori");
		var bombur = scn.GetFreepsCard("bombur");
		var bilbo = scn.GetRingBearer();
		var uruk = scn.GetShadowCard("uruk");
		var fury = scn.GetShadowCard("fury");

		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(dori);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.AttachCardsTo(bilbo, bombur);
		scn.MoveMinionsToTable(uruk);
		scn.AttachCardsTo(uruk, fury);
		assertAttachedTo(beorn, balin);
		assertAttachedTo(bombur, bilbo);
		assertAttachedTo(fury, uruk);
		assertInZone(Zone.SUPPORT, dori);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, uruk);
		scn.FreepsResolveSkirmish(balin);
		// Balin 7 + 2 = 9 against the Uruk's 10 + 1: the Uruk wins.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();
		// No [uruk-hai] minion is wounded and no site can be taken, so the discard of a follower comes unasked.

		// Followers in the support area and borne are offered; Beorn, attached, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(dori, bombur));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beorn));
		scn.ShadowChooseCard(dori);

		assertInDiscard(dori);
		assertAttachedTo(beorn, balin);
		assertAttachedTo(bombur, bilbo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AttachedBeornIsNotOfferedToAShadowPossessionsDiscardAndAFreePeoplesCardMayDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var librarian = scn.GetFreepsCard("librarian");
		var elfarmy = scn.GetFreepsCard("elfarmy");
		var galadriel = scn.GetFreepsCard("galadriel");
		var grond = scn.GetShadowCard("grond");

		scn.MoveCompanionsToTable(balin, librarian);
		scn.MoveAlliesToTable(elfarmy, galadriel);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.MoveCardsToSupportArea(grond);
		scn.AddTokensToCard(grond, 3);
		assertAttachedTo(beorn, balin);
		assertInZone(Zone.SUPPORT, grond, elfarmy, galadriel);
		assertEquals(3, scn.GetCultureTokensOn(grond));

		scn.SkipToPhase(Phase.REGROUP);
		assertAttachedTo(beorn, balin);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("3");
		// Free Peoples cards of cost 3 that are no companions: the two allies are offered; Beorn, attached, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elfarmy, galadriel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beorn));
		scn.ShadowChooseCard(galadriel);
		assertInDiscard(galadriel);
		assertAttachedTo(beorn, balin);

		// A Free Peoples card may discard him: the Librarian's cost.
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(librarian));
		scn.FreepsUseCardAction(librarian);
		assertInDiscard(beorn);
		// No [gandalf] skirmish event in his discard pile: the draw comes unasked.
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishHisPlayerMayTakeAGandalfCardFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var radagast = scn.GetFreepsCard("radagast");
		var glamdring = scn.GetFreepsCard("glamdring");
		var courage = scn.GetFreepsCard("courage");
		var dwalin = scn.GetFreepsCard("dwalin");
		var mySaruman = scn.GetFreepsCard("saruman");
		var hisSaruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(balin);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.MoveCardsToDiscard(radagast, glamdring, courage, dwalin);
		// A [gandalf] card in the other player's discard pile, of a title the player's own piles do not hold.
		scn.MoveCardsToDiscard(hisSaruman);
		scn.MoveCardsToHand(mySaruman);
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(beorn, balin);
		assertInZone(Zone.DISCARD, radagast, glamdring, courage, dwalin, hisSaruman);
		assertInHand(mySaruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);
		// Balin 7 + 2 = 9 against the Runner's 5.
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(beorn));
		scn.FreepsAcceptOptionalTrigger();
		// A [gandalf] character, item and event; not a [dwarven] card, nor the other player's [gandalf] card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, glamdring, courage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwalin, hisSaruman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(glamdring);

		assertInHand(glamdring);
		assertInZone(Zone.DISCARD, radagast, courage, dwalin, hisSaruman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedAtTheFirstWinItIsOfferedAgainAtTheFierceWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var radagast = scn.GetFreepsCard("radagast");
		var courage = scn.GetFreepsCard("courage");
		var runner = scn.GetShadowCard("runner");
		var seeker = scn.GetShadowCard("seeker");

		scn.MoveCompanionsToTable(balin);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.MoveCardsToDiscard(radagast, courage);
		scn.MoveMinionsToTable(runner, seeker);
		assertAttachedTo(beorn, balin);
		assertInZone(Zone.DISCARD, radagast, courage);

		// Balin takes the Runner; the Seeker, fierce, is left for the fierce round.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(beorn));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, radagast, courage);

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balin, seeker);
		scn.FreepsResolveSkirmish(balin);
		// Balin 9 against the Seeker's 6.
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(beorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(courage);
		assertInHand(courage);
		assertInZone(Zone.DISCARD, radagast);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnotherCompanionWinsOrBearerLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var radagast = scn.GetFreepsCard("radagast");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(balin, dwalin);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.MoveCardsToDiscard(radagast);
		scn.MoveMinionsToTable(runner, troop);
		assertAttachedTo(beorn, balin);
		assertInZone(Zone.DISCARD, radagast);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dwalin, runner}, new PhysicalCardImpl[]{balin, troop});

		// Dwalin, who does not bear Beorn, beats the Runner: nothing.
		scn.FreepsResolveSkirmish(dwalin);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(beorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Balin, 9, loses to the Troop's 13 and lives: nothing.
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.FreepsResolveSkirmish(balin);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(beorn));
		assertInZone(Zone.DISCARD, radagast);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGandalfCardInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(balin);
		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.MoveCardsToDiscard(dwalin);
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(beorn, balin);
		assertInZone(Zone.DISCARD, dwalin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);
		scn.PassCurrentPhaseActions();

		// Balin won, and the only card in the pile is a [dwarven] one.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(beorn));
		assertInZone(Zone.DISCARD, dwalin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
