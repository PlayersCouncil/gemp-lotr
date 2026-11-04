package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "18_61");

					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10, vitality 3; fierce only at a battleground (none on this path)
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					put("marksman", "1_176");   // Goblin Marksman: a second archer minion, Archer its whole text

					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, vitality 2; takes part in archery fire and skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "18_61");
					put("corps", "11_73");      // Corps of Harad: [men] Man with no text away from a battleground
					put("guard", "1_7");        // Dwarf Guard: vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Corsair Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [men] minion.
		 * 	Bearer is an <b>archer</b>.
		 * 	Each time a companion is killed during the archery phase, you may control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Corsair Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAMenMinionAndIsThenAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToHand(bow);
		scn.MoveMinionsToTable(corps, ted, brigand);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, bow);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(corps, Keyword.ARCHER));
		assertTrue(scn.ShadowPlayAvailable(bow));
		scn.ShadowPlayCard(bow);
		// [men] minions of two races are offered; a Man minion of another culture is not, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		assertAttachedTo(bow, corps);
		assertTrue(scn.HasKeyword(corps, Keyword.ARCHER));
		// Ted Sandyman, another [men] minion, bears nothing and is no archer.
		assertFalse(scn.HasKeyword(ted, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCompanionKilledInTheArcheryPhaseOffersASiteAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var marksman = scn.GetShadowCard("marksman");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Both markers on site 4: sites 1, 2 and 3 are behind every marker as the fellowship moves on to site 5.
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(guard, aragorn);
		// Each has one vitality left.
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(corps, marksman);
		scn.AttachCardsTo(corps, bow);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertAttachedTo(bow, corps);

		// The Bow's bearer and the Goblin Marksman: two archery wounds, placed by the Free Peoples player.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// A [dwarven] Dwarf.
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// A [gondor] Man.
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheSiteOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, bow);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertAttachedTo(bow, corps);

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACompanionKilledInASkirmishPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, bow);
		assertAttachedTo(bow, corps);

		// The bearer's archery wound goes on Frodo, who lives.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, corps);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// 10 + 1 for the Bow against 4: the Guard is overwhelmed in the skirmish phase.
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void AnAllyKilledInTheArcheryPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var thrarin = scn.GetFreepsCard("thrarin");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, bow);
		assertAttachedTo(bow, corps);

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in archery fire, and has one vitality left.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(thrarin);
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WithASiteThatCanBeControlledACompanionKilledInTheArcheryPhaseOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, bow);

		// The Bow's bearer is the one archer: one archery wound, which the Free Peoples player places.
		scn.SkipToArcheryWounds();
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsChooseCard(guard);
		// His second wound.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledACompanionKilledInTheArcheryPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var bow = scn.GetShadowCard("bow");
		var corps = scn.GetShadowCard("corps");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, bow);

		scn.SkipToArcheryWounds();
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.FreepsControls(site1));

		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
