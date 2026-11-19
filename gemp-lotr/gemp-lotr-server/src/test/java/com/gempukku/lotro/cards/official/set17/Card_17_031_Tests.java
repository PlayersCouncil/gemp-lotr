package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("narsil", "17_31");
					put("elendil", "9_32");     // Elendil, The Tall: [gondor] Man companion, strength 8, vitality 5; his abilities are used only on purpose
					put("isildur", "9_33");     // Isildur, Bearer of Heirlooms: [gondor] Man companion, strength 7, vitality 3; his texts read only a Ring-bearer Isildur and [gondor] knights
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion of another title; his skirmish ability is used only on purpose
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession, for Frodo; no text

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("miscreant", "11_131"); // Orc Miscreant: [orc] Orc, strength 7, vitality 3; a regroup ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, strength 8 +1 for each companion, vitality 3
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
					put("banner", "6_98");      // Banner of the Eye: [sauron] possession on a [sauron] Orc; its trigger answers only its bearer's win
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the support area; its texts need Shelob or a bearer
					put("poison2", "8_28");
					put("stone", "9_47");       // Ithil Stone: a [sauron] ARTIFACT in the support area; its texts act only in the Shadow phase
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] CONDITION in the support area; nothing while no Uruk-hai is in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NarsilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Narsil, Forged by Telchar
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Strength: +3
		 * Game Text: Bearer must be Elendil or Isildur. Bearer is damage +1.
		 * 	Each time bearer wins a skirmish, you may exert him to discard a Shadow possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("narsil");

		assertEquals("Narsil", card.getBlueprint().getTitle());
		assertEquals("Forged by Telchar", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnElendilOrIsildurAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var elendil = scn.GetFreepsCard("elendil");
		var isildur = scn.GetFreepsCard("isildur");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(elendil, isildur, boromir);
		scn.MoveCardsToHand(narsil);

		scn.StartGame();

		assertInHand(narsil);
		assertEquals(0, scn.GetKeywordCount(elendil, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(isildur, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(narsil));

		scn.FreepsPlayCard(narsil);
		// Elendil and Isildur, each a [gondor] Man; not Boromir, a [gondor] Man of another title, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elendil, isildur));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(isildur);

		assertAttachedTo(narsil, isildur);
		assertEquals(1, scn.GetKeywordCount(isildur, Keyword.DAMAGE));
		// Elendil, who could bear it and does not, gains nothing.
		assertEquals(0, scn.GetKeywordCount(elendil, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilElendilIsInPlayAndThenPlaysOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var elendil = scn.GetFreepsCard("elendil");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(narsil);

		scn.StartGame();

		// Only Boromir and Frodo: no bearer.
		assertInHand(narsil);
		assertFalse(scn.FreepsPlayAvailable(narsil));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCompanionsToTable(elendil);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(narsil));
		assertEquals(0, scn.GetKeywordCount(elendil, Keyword.DAMAGE));

		// Elendil is the only bearer, and Narsil goes on him unasked.
		scn.FreepsPlayCard(narsil);
		assertAttachedTo(narsil, elendil);
		assertEquals(1, scn.GetKeywordCount(elendil, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerWinsHeMayExertToDiscardAShadowPossessionAndAnotherElendilWinningIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var elendil = scn.GetFreepsCard("elendil");
		var isildur = scn.GetFreepsCard("isildur");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(elendil, isildur);
		scn.AttachCardsTo(isildur, narsil);
		scn.AttachCardsTo(frodo, sword);

		var runner = scn.GetShadowCard("runner");
		var miscreant = scn.GetShadowCard("miscreant");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		var poison = scn.GetShadowCard("poison");
		var stone = scn.GetShadowCard("stone");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(poison, stone, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, miscreant, chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(narsil, isildur);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(banner, chieftain);
		assertInZone(Zone.SUPPORT, poison, stone, armory);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elendil, runner},
				new PhysicalCardImpl[]{isildur, miscreant});
		// The Chieftain is left unassigned, and stands by with his Banner.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(elendil);
		scn.PassSkirmishActions();
		// Elendil, 8 against 5, wins without Narsil: nothing asks.
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(narsil));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(isildur);
		assertEquals(0, scn.GetWoundsOn(isildur));
		scn.PassSkirmishActions();
		// Isildur, 7 + 3 against 7, wins; the Miscreant takes 1 wound and 1 more for the damage bonus.
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narsil));
		scn.FreepsChooseOptionalTrigger(narsil);

		assertEquals(1, scn.GetWoundsOn(isildur));
		// A Shadow possession borne and one in a support area, of two cultures; not a Shadow artifact, a Shadow
		// condition, or a Free Peoples possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(banner, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stone, armory, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(banner);

		assertInZone(Zone.DISCARD, banner);
		assertInZone(Zone.SUPPORT, poison, stone, armory);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCompanionsToTable(isildur);
		scn.AttachCardsTo(isildur, narsil);

		var troop = scn.GetShadowCard("troop");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, poison);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, troop);
		scn.FreepsResolveSkirmish(isildur);
		assertEquals(0, scn.GetWoundsOn(isildur));
		scn.PassSkirmishActions();

		// 10 against 13: he loses, takes his wound and lives, able to exert again.
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertInZone(Zone.FREE_CHARACTERS, isildur);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(narsil));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedAndNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCompanionsToTable(isildur);
		scn.AttachCardsTo(isildur, narsil);

		var miscreant = scn.GetShadowCard("miscreant");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		assertInZone(Zone.SUPPORT, poison);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, miscreant);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(isildur));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narsil));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(isildur));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenAnExhaustedBearerWinsNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCompanionsToTable(isildur);
		scn.AttachCardsTo(isildur, narsil);

		var miscreant = scn.GetShadowCard("miscreant");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(isildur, 2);
		assertEquals(2, scn.GetWoundsOn(isildur));
		scn.MoveMinionsToTable(miscreant);
		assertInZone(Zone.SUPPORT, poison);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, miscreant);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();

		// 10 against 7: he wins, but cannot exert.
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(narsil));
		assertEquals(2, scn.GetWoundsOn(isildur));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoShadowPossessionInPlayBearerMayStillExertForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCompanionsToTable(isildur);
		scn.AttachCardsTo(isildur, narsil);

		var miscreant = scn.GetShadowCard("miscreant");
		var stone = scn.GetShadowCard("stone");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(stone, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		assertInZone(Zone.SUPPORT, stone, armory);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, miscreant);
		scn.FreepsResolveSkirmish(isildur);
		scn.PassSkirmishActions();

		// Only a Shadow artifact and a Shadow condition are in play. The exertion is a cost, so the offer stands.
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narsil));
		scn.FreepsChooseOptionalTrigger(narsil);

		assertEquals(1, scn.GetWoundsOn(isildur));
		assertInZone(Zone.SUPPORT, stone, armory);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsAnsweredAgainInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var elendil = scn.GetFreepsCard("elendil");
		scn.MoveCompanionsToTable(elendil);
		scn.AttachCardsTo(elendil, narsil);

		var rider = scn.GetShadowCard("rider");
		var poison = scn.GetShadowCard("poison");
		var poison2 = scn.GetShadowCard("poison2");
		scn.MoveCardsToSupportArea(poison, poison2);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		assertInZone(Zone.SUPPORT, poison, poison2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elendil, rider);
		scn.FreepsResolveSkirmish(elendil);
		assertEquals(0, scn.GetWoundsOn(elendil));
		scn.PassSkirmishActions();

		// 8 + 3 against the Rider's 8 + 2 (Frodo and Elendil): Elendil wins, and the Rider takes 2 wounds and lives.
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narsil));
		scn.FreepsChooseOptionalTrigger(narsil);
		assertEquals(1, scn.GetWoundsOn(elendil));
		assertTrue(scn.FreepsHasCardChoiceAvailable(poison, poison2));
		scn.FreepsChooseCard(poison);
		assertInZone(Zone.DISCARD, poison);
		assertInZone(Zone.SUPPORT, poison2);
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(elendil, rider);
		scn.FreepsResolveSkirmish(elendil);
		scn.PassFierceSkirmishActions();

		// He wins again, and is asked again.
		assertInZone(Zone.DISCARD, rider);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narsil));
		scn.FreepsChooseOptionalTrigger(narsil);

		// The second Spider Poison is the only Shadow possession left and is discarded unasked.
		assertEquals(2, scn.GetWoundsOn(elendil));
		assertInZone(Zone.DISCARD, poison2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
