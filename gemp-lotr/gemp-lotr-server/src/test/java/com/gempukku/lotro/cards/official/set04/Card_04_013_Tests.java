package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "4_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPillagerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "4_13");
					put("pillager2", "4_13");
					// Hides: a [dunland] possession in the support area, a Shadow possession.  "Response: If a
					// [dunland] Man is about to take a wound, remove (2) or discard this possession to prevent that
					// wound."
					put("hides", "4_19");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man with no text
					// Anger: [dunland] Skirmish event, "Spot 3 [dunland] minions or a site you control to make a
					// [dunland] Man fierce until the regroup phase."
					put("anger", "4_3");

					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession on Gimli; a bar on overwhelming only
					put("sam", "1_311");        // Sam, Son of Hamfast: his texts are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a bar on discarding [shire] tales only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a Hobbit ally; a fellowship ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, no text but its bearer
					put("sword2", "1_299");
					// Old Toby: a [shire] pipeweed possession, played to the support area, its only text a trigger on
					// its own play: a Free Peoples possession that no one bears.
					put("toby", "1_305");
					// The Shards of Narsil: a [gondor] artifact in the support area, a Free Peoples item that is no
					// possession; fellowship abilities only.
					put("shards", "3_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may discard a Free Peoples possession (or 2 possessions if you spot 6 companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillager");

		assertEquals("Dunlending Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishOffersToDiscardOneFreePeoplesPossessionWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPillagerScenario();

		var pillager = scn.GetShadowCard("pillager");
		var pillager2 = scn.GetShadowCard("pillager2");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		var shards = scn.GetFreepsCard("shards");
		var toby = scn.GetFreepsCard("toby");
		// Frodo and four more: five companions, and an ally beside them who is no companion.
		scn.MoveCompanionsToTable(knight, gimli, sam, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(bilbo, sword);
		scn.MoveCardsToSupportArea(shards, toby);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(pillager, pillager2);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);
		assertInZone(Zone.SUPPORT, shards, toby, hides);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));

		// 9 against 6: the Pillager wins.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		// "This minion": the other Pillager, unassigned, won nothing and offers nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager2));
		scn.ShadowAcceptOptionalTrigger();

		// A Free Peoples possession, on a companion, on an ally and in the support area; not the Free Peoples
		// artifact, nor the Shadow possession.  One, not two: five companions are spotted, and the ally is none.
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, sword, toby));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shards, hides));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(toby);
		assertInDiscard(toby);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);
		assertInZone(Zone.SUPPORT, shards, hides);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithSixCompanionsItDiscardsTwoFreePeoplesPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPillagerScenario();

		var pillager = scn.GetShadowCard("pillager");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var shards = scn.GetFreepsCard("shards");
		// Frodo and five more: six companions.
		scn.MoveCompanionsToTable(knight, gimli, sam, aragorn, pippin);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(bilbo, sword);
		scn.AttachCardsTo(sam, sword2);
		scn.MoveCardsToSupportArea(shards);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(pillager);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);
		assertAttachedTo(sword2, sam);
		assertInZone(Zone.SUPPORT, shards, hides);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.FreepsResolveSkirmish(knight);

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		scn.ShadowAcceptOptionalTrigger();

		// Two of the three Free Peoples possessions, exactly: the branch in parentheses is taken when it can be.
		// Neither the artifact nor the Shadow possession is among them.
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, sword, sword2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shards, hides));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(armor, sword2);
		assertInDiscard(armor, sword2);
		assertAttachedTo(sword, bilbo);
		assertInZone(Zone.SUPPORT, shards, hides);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPillagerScenario();

		var pillager = scn.GetShadowCard("pillager");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(knight, gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		assertAttachedTo(armor, gimli);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.FreepsResolveSkirmish(knight);

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesPossessionInPlayWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPillagerScenario();

		var pillager = scn.GetShadowCard("pillager");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(shards);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(pillager);
		assertInZone(Zone.SUPPORT, shards, hides);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.FreepsResolveSkirmish(knight);

		// He wins, with a Free Peoples artifact and a Shadow possession in play and no Free Peoples possession:
		// a yes would discard nothing (Q-0034).
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, shards, hides);
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPillagerScenario();

		var pillager = scn.GetShadowCard("pillager");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(knight, gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(pillager);
		assertInZone(Zone.SUPPORT, hides);
		assertAttachedTo(armor, gimli);
		// The Knight at 10 beats the Pillager's 9 without overwhelming him (18).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(pillager));
		assertAttachedTo(armor, gimli);

		scn.PassCurrentPhaseActions();
		// Hides saves the Pillager from the wound for losing, so that he is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(0, scn.GetWoundsOn(pillager));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningTheNormalAndTheFierceSkirmishDiscardsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPillagerScenario();

		var pillager = scn.GetShadowCard("pillager");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var anger = scn.GetShadowCard("anger");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(knight, gimli);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(bilbo, sword);
		scn.MoveCardsToHand(anger);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager, brigand, savage);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		// Anger, spotting the three [dunland] minions, makes the Pillager fierce.
		scn.FreepsPass();
		scn.ShadowPlayCard(anger);
		scn.ShadowChooseCard(pillager);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(armor);
		assertInDiscard(armor);
		assertAttachedTo(sword, bilbo);

		// The fierce skirmish: the Pillager wins again, and the last Free Peoples possession goes.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, pillager);
		scn.FreepsResolveSkirmish(knight);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(armor, sword);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
