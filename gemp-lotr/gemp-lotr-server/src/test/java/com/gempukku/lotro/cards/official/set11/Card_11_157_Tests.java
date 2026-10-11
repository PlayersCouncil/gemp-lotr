package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					// Advance Scout: [sauron] Orc, vitality 2.  "Shadow: Exert or discard this minion to make each
					// [sauron] minion you spot roaming until the regroup phase."
					put("scout", "10_78");
					// Morgul Slayer: [sauron] Orc, vitality 2.  "Regroup: Exert this minion to wound a companion
					// (except the Ring-bearer)."
					put("slayer1", "3_93");
					put("slayer2", "3_93");
					put("slayer3", "3_93");
					// His Cruelty and Malice: [sauron] condition in the support area, cost 1.  "Regroup: Exert a
					// [sauron] minion to make the Free Peoples player discard the top card from his or her draw deck."
					put("cruelty", "3_91");
					// Massing in the East: [sauron] Regroup event, cost 1.  "Exert a [sauron] minion to discard a
					// card from the top of the Free Peoples player's draw deck for each of these races you can spot
					// in the fellowship: Dwarf, Elf, Man, and Wizard."
					put("massing", "3_92");
					// Barbarous Orc: [orc] Orc, strength 7, vitality 2.  "Skirmish: Exert another [orc] minion to
					// make this minion strength +2 (or +4 if this minion is skirmishing a companion who has
					// resistance 4 or less)."
					put("orc1", "11_107");
					put("orc2", "11_107");
					// Aragorn, Ranger of the North: a Man of [gondor], strength 8.  "Maneuver: Exert Aragorn to make
					// him defender +1 until the regroup phase."
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					// Úlairë Nelya, Lieutenant of Morgul: fierce, vitality 3.  "Shadow: Exert Úlairë Nelya and
					// spot an opponent's site to replace it with your site of the same number."
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHobbitSetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					// Lavastone Giant (The Hobbit): [moria] Giant, strength 14, vitality 4.  "Archery:" the Giant
					// exerts as the cost, a card is revealed at random from the Free Peoples hand, and if it is no
					// Shadow card the Giant exerts again as the effect.  The one minion whose own ability exerts it
					// as an effect.
					put("giant", "33_44");
					// Dwarf Guard: a Free Peoples card, so the one card revealed from the hand is no Shadow card.
					// It is never played.
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExertScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, for a second Weary
					// Weary: [isengard] condition, cost 1.  "Plays on a companion or ally.  Limit 1 per character."
					// Its trigger needs an [isengard] tracker.
					put("weary1", "4_212");
					put("weary2", "4_212");
					// Orthanc Warrior: [isengard] Uruk-hai, vitality 2, so one exertion exhausts it; Damage +1 is
					// its whole text
					put("warrior", "4_165");
					// Southron Explorer: [raider] Man, vitality 3.  Its regroup ability (exert it and remove (5) to
					// take control of a site) is not used.
					put("explorer", "4_250");
					// His Cruelty and Malice: [sauron] condition played to the support area, cost 1, not unique.
					// Its regroup ability exerts a [sauron] minion, and there is none.
					put("cruelty1", "3_91");
					put("cruelty2", "3_91");
					// Spider Poison: a [gollum] Shadow POSSESSION played to the support area, cost 1.  Its skirmish
					// ability needs Shelob.
					put("poison", "8_28");
					// Treebeard, Keeper of the Watchwood: vitality 4.  "Regroup: Exert Treebeard twice to play a
					// [gandalf] condition from your discard pile and make an opponent discard one of his or her
					// conditions."  To play, spot 3 [gandalf] companions: he is put on the table.
					put("treebeard", "10_18");
					// Ship of Smoke: [gandalf] condition played to the support area, cost 0.  Its trigger needs
					// Gandalf.
					put("ship", "18_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, and [gondor]
					// Merry, Master Holbytla: a [rohan] Hobbit.  He is a Man only beside Éowyn or with 3 [rohan]
					// companions in play.
					put("merry", "103_85");
					put("guard", "5_83");       // Household Guard: [rohan] Man, a companion, cost 2; Valiant is its whole text
					// Guma, Plains Farmer: [rohan] Man, an ally (home 4T).  His fellowship ability heals a [rohan]
					// Man, and nobody is wounded.
					put("guma", "4_277");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RushofSteedsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rush of Steeds
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [rohan] Man.
		 * Each time a Shadow condition is played, you may exert a minion.
		 * <b>Response:</b> If a minion exerts as a cost of its special ability, discard this condition to prevent that and return that minion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rush");

		assertEquals("Rush of Steeds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARohanManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var rush = scn.GetFreepsCard("rush");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToHand(rush, guard);

		scn.StartGame();

		// A Man who is not [rohan] and a [rohan] companion who is no Man.
		assertInZone(Zone.HAND, rush);
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertFalse(scn.FreepsPlayAvailable(rush));

		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(rush));

		scn.FreepsPlayCard(rush);
		// 2 for the Guard + 3
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, rush);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var rush = scn.GetFreepsCard("rush");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(rush);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(rush));

		scn.FreepsPlayCard(rush);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, rush);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachShadowConditionPlayedOffersAnExertionOfAMinionOfTheFreePeoplesPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertScenario();

		var frodo = scn.GetRingBearer();
		var rush = scn.GetFreepsCard("rush");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rush);

		var weary = scn.GetShadowCard("weary1");
		var cruelty1 = scn.GetShadowCard("cruelty1");
		var cruelty2 = scn.GetShadowCard("cruelty2");
		var warrior = scn.GetShadowCard("warrior");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(weary, cruelty1, cruelty2);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(explorer));

		// An [isengard] condition played on a companion: offered, and declined.
		scn.ShadowPlayCard(weary);
		scn.ShadowChooseCard(frodo);
		assertAttachedTo(weary, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [sauron] condition played to the support area: offered again, and taken.  Either minion may be
		// chosen, and no companion.
		scn.ShadowPlayCard(cruelty1);
		assertInZone(Zone.SUPPORT, cruelty1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, explorer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(warrior));
		// That exertion was Rush of Steeds' own doing and the cost of nothing: its Response is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, rush);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// And at a third condition, the same minion again.
		scn.ShadowPlayCard(cruelty2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(explorer);
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowPossessionAndAFreePeoplesConditionPlayedBringNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertScenario();

		var frodo = scn.GetRingBearer();
		var rush = scn.GetFreepsCard("rush");
		var treebeard = scn.GetFreepsCard("treebeard");
		var ship = scn.GetFreepsCard("ship");
		scn.MoveCompanionsToTable(treebeard);
		scn.MoveCardsToSupportArea(rush);
		scn.MoveCardsToDiscard(ship);

		var poison = scn.GetShadowCard("poison");
		var weary = scn.GetShadowCard("weary1");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(poison, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// A Shadow card that is no condition, played to the support area with a minion there to exert.
		scn.ShadowPlayCard(poison);
		assertInZone(Zone.SUPPORT, poison);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow condition played in that same phase is answered.
		scn.ShadowPlayCard(weary);
		scn.ShadowChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsDeclineOptionalTrigger();

		// A Free Peoples condition, played by Treebeard in the regroup phase with the minion still on the
		// table.  Weary, the Shadow player's one condition, is what he discards for it.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.DISCARD, ship);
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertTrue(scn.FreepsActionAvailable(treebeard));

		scn.FreepsUseCardAction(treebeard);
		assertEquals(2, scn.GetWoundsOn(treebeard));
		assertInZone(Zone.SUPPORT, ship);
		assertInZone(Zone.DISCARD, weary);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AMinionWhoChoosesToExertForItsOwnAbilityIsSentBackToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.MoveCardsToSupportArea(rush);

		var scout = scn.GetShadowCard("scout");
		var slayer = scn.GetShadowCard("slayer1");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, slayer);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);
		scn.ShadowChooseOption("exert");

		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, rush);
		// The Scout is in the hand of the player who owns it, and the other minion stays where it was.
		assertInZone(Zone.HAND, scout);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, slayer);
		assertEquals(0, scn.GetWoundsOn(slayer));
		// The Scout's ability was not paid for: nobody is asked which [sauron] minions are to be roaming.
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnExertionPaidForAConditionsAbilityOrForAnEventIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetFreepsCard("rush");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rush);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var slayer3 = scn.GetShadowCard("slayer3");
		var cruelty = scn.GetShadowCard("cruelty");
		var massing = scn.GetShadowCard("massing");
		scn.MoveCardsToSupportArea(cruelty);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2, slayer3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// A minion exerts as the cost of a condition's special ability, which is not its own.
		int discarded = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetWoundsOn(slayer1));
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(slayer1);
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(discarded + 1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, rush);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// A minion exerts as the cost of an event.  Aragorn is the one Man, Dwarf, Elf or Wizard: one card.
		assertEquals(0, scn.GetWoundsOn(slayer2));
		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCard(slayer2);
		assertEquals(1, scn.GetWoundsOn(slayer2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(discarded + 2, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, rush);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// The third Slayer exerts for its own ability, in the same phase: that is answered.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowUseCardAction(slayer3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, rush);
		assertInZone(Zone.HAND, slayer3);
		// The other two keep their places and their wounds, and Aragorn is not wounded.
		assertInZone(Zone.SHADOW_CHARACTERS, slayer1, slayer2);
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(1, scn.GetWoundsOn(slayer2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionExertedForAnotherMinionsAbilityAndACompanionExertingForHisOwnAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetFreepsCard("rush");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rush);

		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");

		scn.StartGame();
		scn.MoveMinionsToTable(orc1, orc2);
		scn.SkipToPhase(Phase.MANEUVER);

		// A companion exerts as the cost of his own special ability.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, rush);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The first Orc's ability exerts the second: a minion exerts for a minion's ability, and not its own.
		assertEquals(7, scn.GetStrength(orc1));
		assertEquals(0, scn.GetWoundsOn(orc2));
		assertTrue(scn.ShadowActionAvailable(orc1));
		scn.ShadowUseCardAction(orc1);
		assertEquals(1, scn.GetWoundsOn(orc2));
		assertEquals(0, scn.GetWoundsOn(orc1));
		assertEquals(9, scn.GetStrength(orc1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, orc1, orc2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// "As a cost of its special ability": the Giant's first exertion is the cost and is answered (declined here); its
	// second is the effect of the same ability, and is not.
	@Test
	@Ignore("F-0478: Rush of Steeds' Response (exertsForSpecialAbility) is offered when a minion exerts as the EFFECT of its own special ability (Lavastone Giant 33_44's second exertion), not only as its cost; the trigger never tells a cost from an effect")
	public void AMinionWhoExertsAgainAsTheEffectOfItsOwnAbilityIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitSetScenario();

		var rush = scn.GetFreepsCard("rush");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(rush);
		scn.MoveCardsToHand(guard);

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.MoveMinionsToTable(giant);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(giant));
		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);

		// The first exertion is the cost of the ability.
		assertEquals(1, scn.GetWoundsOn(giant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsDeclineOptionalTrigger();

		// The one card in the Free Peoples hand is shown to the Shadow player; it is no Shadow card, so the
		// Giant exerts a second time, as the effect.
		assertEquals(1, scn.GetWoundsOn(giant));
		scn.ShadowDismissRevealedCards();

		assertEquals(2, scn.GetWoundsOn(giant));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, giant);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	// Ketura on Q-0038: a cost that is prevented is "no longer considered paid", and what it was to buy does not
	// happen (CR 4.2 "cost": "If all the costs cannot be paid, that card has no effect").
	@Test
	public void TheAbilityWhoseExertionWasPreventedDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.MoveCardsToSupportArea(rush);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		var freepsSite1 = scn.GetFreepsSite(1);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		// The fellowship is at site 2, which the Shadow player laid.  Site 1 behind it is the Free Peoples
		// player's own: the one site of an opponent's that Nelya can spot.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(freepsSite1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, rush);
		assertInZone(Zone.HAND, nelya);
		// The exertion was the cost of Nelya's ability: site 1 is the card it was.
		assertSame(freepsSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// The control of the test above: the same board with the Response declined.  The site is replaced by the
	// Shadow player's copy of the same site, as the game is built (Q-0226).
	@Test
	public void WithTheResponseDeclinedTheMinionExertsAndItsAbilityIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.MoveCardsToSupportArea(rush);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		assertSame(freepsSite1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered.
	// "Each time a Shadow condition is played, you may exert a minion": with no minion in play who can exert,
	// a yes would do nothing.  The game used to offer it all the same (F-0373).
	@Test
	public void WithNoMinionWhoCanExertTheExertionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertScenario();

		var frodo = scn.GetRingBearer();
		var rush = scn.GetFreepsCard("rush");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rush);

		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(weary1, weary2);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// A Shadow condition is played while the Warrior can exert: the offer is made, and taken.
		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.ShadowPlayCard(weary1);
		scn.ShadowChooseCard(frodo);
		assertAttachedTo(weary1, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.IsExhausted(warrior));

		// A second is played, on the one companion who bears none: the only minion is exhausted, and nothing
		// is asked of the Free Peoples player.
		scn.ShadowPlayCard(weary2);
		assertAttachedTo(weary2, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rush));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
