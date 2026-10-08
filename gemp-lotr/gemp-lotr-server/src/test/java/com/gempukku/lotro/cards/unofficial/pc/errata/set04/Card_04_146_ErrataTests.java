package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_146_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comedown", "54_146");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde: Elf ally, home site 3, vitality 2; has the text of site 3 while it can be spotted
					put("celeborn", "1_34");    // Celeborn: Elf ally, home site 6, vitality 3; a fellowship ability used only on purpose
					put("orophin", "1_56");     // Orophin: Elf ally and archer, home site 6; an archery ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, a Man, vitality 1, home site 3T; a maneuver ability used only on purpose
					put("citywall", "5_33");    // City Wall: [gondor] fortification, cost 1, strength -3 to a bearer; a skirmish ability used only on purpose
					put("citywall2", "5_33");
					put("parapet", "5_87");     // Parapet: [rohan] fortification, cost 1; an archery ability used only on purpose
					put("garrison", "6_52");    // Garrison of Osgiliath: [gondor] fortification, cost 2; its when-played tokens do nothing when cheated in
					put("strong", "7_122");     // Strong and Old: each of your [gondor] fortifications costs 1 less; a response about site control only
					put("rallypoint", "5_38");  // Rally Point: [gondor] condition in the support area that is not a fortification; a regroup ability used only on purpose
					put("overseer", "3_65");    // Orc Overseer: [isengard] Orc minion, vitality 4; a regroup ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, vitality 3, damage +1 and nothing else; not an archer
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] Uruk-hai archer, damage +1 and nothing else
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] Uruk-hai archer; its when-played token does nothing when cheated in
					put("bowmen", "4_138");     // Band of Uruk Bowmen: [isengard] Uruk-hai archer; +1 archery total only while its player controls a site (none here)
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComeDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Come Down
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Maneuver: If you can spot an [isengard] minion, spot a fortification.  Make the Free Peoples
		 * player distribute X + 1 wounds among your [isengard] minions, where X is its twilight cost. Discard that
		 * fortification.
		 * Archery: Spot 2 [isengard] archers to make allies take wounds from archery fire instead of companions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("comedown");

		assertEquals("Come Down", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		scn.MoveCardsToHand(comedown);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(comedown));
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(comedown);
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, comedown);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverSpotsAFortificationAndTheFreePeoplesPlayerPlacesTwoWoundsAmongIsengardMinionsForACostOfOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var overseer = scn.GetShadowCard("overseer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var citywall = scn.GetFreepsCard("citywall");
		var citywall2 = scn.GetFreepsCard("citywall2");
		var parapet = scn.GetFreepsCard("parapet");
		var garrison = scn.GetFreepsCard("garrison");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCardsToSupportArea(comedown, citywall, parapet, garrison, rallypoint);

		scn.StartGame();
		scn.HinderCard(garrison);
		scn.MoveMinionsToTable(overseer, savage, troop);
		scn.AttachCardsTo(troop, citywall2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(comedown));
		scn.ShadowUseCardAction(comedown);
		// A hindered card is no fortification (CR 5.0 "hinder").
		assertTrue(scn.ShadowHasCardChoiceAvailable(citywall, citywall2, parapet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(garrison, rallypoint));
		scn.ShadowChooseCard(parapet);

		// X = 1 (Parapet's cost): 2 wounds, each placed by the Free Peoples player, both on one minion if he likes.
		assertTrue(scn.FreepsHasCardChoiceAvailable(overseer, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasCardChoiceAvailable(overseer, savage));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(overseer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(parapet);
		assertInZone(Zone.SUPPORT, citywall, garrison);
		assertAttachedTo(citywall2, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AFortificationCostingTwoMakesThreeWoundsAndOneBorneByAMinionIsDiscardedFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetFreepsCard("strong");
		var comedown = scn.GetShadowCard("comedown");
		var overseer = scn.GetShadowCard("overseer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var citywall = scn.GetFreepsCard("citywall");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCardsToSupportArea(comedown, garrison, strong);

		scn.StartGame();
		scn.MoveMinionsToTable(overseer, savage, troop);
		scn.AttachCardsTo(troop, citywall);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(comedown);
		scn.ShadowChooseCard(garrison);
		// X = 2, Garrison's printed cost, whatever Strong and Old does to the cost of a [gondor] fortification
		// being played (CR 4.2 "twilight cost"): 3 wounds.
		scn.FreepsChooseCard(overseer);
		scn.FreepsChooseCard(overseer);
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(overseer));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(garrison);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Again, on the City Wall a minion bears: X = 1, 2 wounds.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(comedown));
		scn.ShadowUseCardAction(comedown);
		scn.FreepsChooseCard(savage);
		scn.FreepsChooseCard(overseer);
		assertEquals(3, scn.GetWoundsOn(overseer));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(citywall);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverIsNotOfferedWithoutAFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var overseer = scn.GetShadowCard("overseer");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCardsToSupportArea(comedown, rallypoint);

		scn.StartGame();
		scn.MoveMinionsToTable(overseer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
	}

	@Test
	public void ManeuverIsNotOfferedWithoutAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var troop = scn.GetShadowCard("troop");
		var citywall = scn.GetFreepsCard("citywall");
		// Come Down itself is an [isengard] card in play that is not a minion.
		scn.MoveCardsToSupportArea(comedown, citywall);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
		assertInZone(Zone.SUPPORT, citywall);
	}

	@Test
	public void ArcheryWithTwoIsengardArchersSendsArcheryWoundsToAlliesAtHomeOrAwayAndNotCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var celeborn = scn.GetFreepsCard("celeborn");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, celeborn, merchant);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin, crossbow);
		// The fellowship moves to site 3, Silinde's home; Celeborn's is site 6.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(comedown));
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		// Minion archery total 2, both placed by the Free Peoples player on allies. The Gondorian Merchant (a [gondor]
		// Man, home on the Towers sites) is offered beside the two Elves; he is not chosen, since one wound would kill him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, celeborn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(celeborn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, celeborn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		scn.FreepsChooseCard(silinde);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryIsNotOfferedWithOneIsengardArcherBesideAMoriaArcherAndAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, marksman, savage);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(comedown));
	}

	@Test
	public void TheAlliesTakeArcheryWoundsOnlyInTheArcheryPhaseItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		scn.PassArcheryActions();
		// Celeborn, the one ally, is the only character the 2 wounds can go to.
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// No skirmishes; the fellowship moves on to site 3, and the archers fire again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	@Ignore("Q-0136: archery wounds the allies cannot take go to companions (CR 4.2 \"instead\"), or are lost")
	public void ArcheryWoundsTheAlliesCannotTakeGoToCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var bowmen = scn.GetShadowCard("bowmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow, bowmen);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		// Minion archery total 3; Silinde, the one ally, takes two and is killed (vitality 2).
		assertInZone(Zone.DEAD, silinde);

		// The third wound cannot go to an ally, so it is placed as it would have been without Come Down.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	@Ignore("Q-0137: an archer ally away from home counts for the fellowship archery total once Come Down is used")
	public void AnArcherAllyAwayFromHomeDoesNotAddToTheFellowshipArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Orophin's home is site 6; the fellowship is at site 2.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.ShadowUseCardAction(comedown);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
