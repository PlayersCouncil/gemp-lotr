package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crack", "6_25");

					put("quickbeam", "6_33");   // Quickbeam, Bregalad: Ent companion, vitality 3; only a cost discount of his own
					put("onod", "17_26");       // Woodland Onod: Ent companion, vitality 4; a skirmish ability used only on purpose
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: Ent ally, vitality 4; a Fellowship ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion who is no Ent
					put("entmoot", "6_29");     // Ent Moot: a Free Peoples condition, borne by an Ent

					put("armory", "1_173");     // Goblin Armory: [moria] condition in the support area; its texts answer [moria] cards only
					put("weary", "4_212");      // Weary: [isengard] condition borne by a companion; its trigger wants an [isengard] tracker
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the support area; a skirmish ability about Shelob
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrackIntoRubbleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Crack Into Rubble
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert 2 Ents to liberate a site and discard a Shadow condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crack");

		assertEquals("Crack Into Rubble", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoEntsToLiberateTheHighestOpponentSiteAndDiscardAShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crack = scn.GetFreepsCard("crack");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var entmoot = scn.GetFreepsCard("entmoot");
		var armory = scn.GetShadowCard("armory");
		var weary = scn.GetShadowCard("weary");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(quickbeam, onod, gandalf);
		scn.MoveAlliesToTable(treebeard);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		scn.MoveCardsToSupportArea(armory, poison);
		scn.AttachCardsTo(gandalf, weary);
		scn.AttachCardsTo(onod, entmoot);
		scn.MoveCardsToHand(crack);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		assertInZone(Zone.SUPPORT, armory, poison);
		assertAttachedTo(weary, gandalf);
		assertAttachedTo(entmoot, onod);
		assertInHand(crack);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertEquals(0, scn.GetWoundsOn(onod));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(crack));
		scn.FreepsPlayCard(crack);

		// Two Ent companions and an Ent ally; Gandalf is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(quickbeam, onod, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		scn.FreepsChooseCards(quickbeam, treebeard);
		assertEquals(1, scn.GetWoundsOn(quickbeam));
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(onod));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Shadow conditions of two cultures, in the support area and borne; not a Shadow possession, not a Free
		// Peoples condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, entmoot));
		scn.FreepsChooseCard(weary);

		// Site 2 is the highest site an opponent controls; site 3 is the Free Peoples player's own.
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertInDiscard(weary, crack);
		assertInZone(Zone.SUPPORT, armory, poison);
		assertAttachedTo(entmoot, onod);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheConditionIsStillDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crack = scn.GetFreepsCard("crack");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var armory = scn.GetShadowCard("armory");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(quickbeam, onod);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.MoveCardsToSupportArea(armory, poison);
		scn.MoveCardsToHand(crack);

		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, armory, poison);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(quickbeam));
		assertEquals(0, scn.GetWoundsOn(onod));
		scn.FreepsPlayCard(crack);

		// Two Ents for two exertions, and one Shadow condition: nothing is asked.
		assertEquals(1, scn.GetWoundsOn(quickbeam));
		assertEquals(1, scn.GetWoundsOn(onod));
		assertTrue(scn.FreepsControls(site1));
		assertInDiscard(armory, crack);
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoShadowConditionTheSiteIsStillLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crack = scn.GetFreepsCard("crack");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var entmoot = scn.GetFreepsCard("entmoot");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(quickbeam, onod);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToSupportArea(poison);
		scn.AttachCardsTo(onod, entmoot);
		scn.MoveCardsToHand(crack);

		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, poison);
		assertAttachedTo(entmoot, onod);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsPlayAvailable(crack));
		scn.FreepsPlayCard(crack);

		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertInDiscard(crack);
		assertInZone(Zone.SUPPORT, poison);
		assertAttachedTo(entmoot, onod);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneEntAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crack = scn.GetFreepsCard("crack");
		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(quickbeam, onod, gandalf);
		scn.MoveAlliesToTable(treebeard);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(crack);
		// Quickbeam (vitality 3) and Onod (vitality 4) exhausted; Treebeard and Gandalf could exert.
		scn.AddWoundsToChar(quickbeam, 2);
		scn.AddWoundsToChar(onod, 3);

		assertEquals(2, scn.GetWoundsOn(quickbeam));
		assertEquals(3, scn.GetWoundsOn(onod));
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, armory);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(crack);
		assertFalse(scn.FreepsPlayAvailable(crack));
	}
}
