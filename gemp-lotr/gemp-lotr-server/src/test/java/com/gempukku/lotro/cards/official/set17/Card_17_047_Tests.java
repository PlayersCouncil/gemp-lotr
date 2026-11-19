package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brand", "17_47");
					put("brand2", "17_47");
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, strength 7, vitality 1; a regroup ability only
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, strength 8, vitality 2; a maneuver ability only
					put("savage", "17_53");     // Stampeding Savage: [men] Man, strength 8; a maneuver ability only
					put("haradrim", "11_74");   // Detachment of Haradrim: [men] Man, strength 12, vitality 3; a maneuver ability only
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), strength 8.
					// "Each [men] minion bearing a possession is strength +2"; his skirmish ability is never used here.
					put("saruman", "17_48");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					put("poleaxe", "11_95");    // Poleaxe: [men] hand weapon on a [men] minion; a skirmish ability never used here

					put("armor", "17_41");      // Ceremonial Armor: [men] possession in the support area; it answers [men] minions discarded or wounded, neither of which happens here
					put("oath", "17_66");       // Wildman's Oath: [men] possession in the support area; a skirmish ability never used here
					put("hides", "4_19");       // Hides: a [dunland] possession in the support area; it answers [dunland] Men about to take wounds
					put("companies", "12_58");  // Countless Companies: a [men] condition in the support area; it answers [men] possessions discarded

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PrimitiveBrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Primitive Brand
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [men] minion.
		 * Each time bearer wins a skirmish, you may transfer this possession to another [men] minion. If you do, stack a [men] minion from play on a [men] possession in your support area.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("brand");

		assertEquals("Primitive Brand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMenMinionOfEitherRaceAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetShadowCard("brand");
		var hillsman = scn.GetShadowCard("hillsman");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(brand);
		assertInHand(brand);

		scn.StartGame();
		scn.MoveMinionsToTable(hillsman, saruman, brigand);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(brand));
		scn.ShadowPlayCard(brand);

		// A [men] Man and a [men] Wizard; not a Man of another culture, nor the Free Peoples player's companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertAttachedTo(brand, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBearerWinsItMayBeTransferredToAnotherMenMinionAndAMenMinionIsStackedOnAMenSupportPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetShadowCard("brand");
		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var brigand = scn.GetShadowCard("brigand");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var hides = scn.GetShadowCard("hides");
		var companies = scn.GetShadowCard("companies");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(hillsman, warrior, saruman, savage, brigand);
		scn.AttachCardsTo(hillsman, brand);
		scn.AttachCardsTo(savage, poleaxe);
		scn.MoveCardsToSupportArea(armor, oath, hides, companies);
		assertAttachedTo(brand, hillsman);
		assertAttachedTo(poleaxe, savage);
		assertInZone(Zone.SUPPORT, armor, oath, hides, companies);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillsman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// 7 + 2 for the Brand + 2 for Saruman's first sentence, against Aragorn's 8.
		assertEquals(11, scn.GetStrength(hillsman));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brand));
		scn.ShadowAcceptOptionalTrigger();

		// Another [men] minion of either race; not the bearer, a Man of another culture, or a [men] minion
		// who already bears a hand weapon (CR 4.2 "transfer": only to a character who may bear it).
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hillsman, brigand, savage));
		scn.ShadowChooseCard(warrior);
		assertAttachedTo(brand, warrior);

		// Where it goes: a [men] possession in the support area, not a [men] condition there, a possession
		// of another culture there, or a [men] possession a minion bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hides, companies, poleaxe, brand));
		scn.ShadowChooseCard(armor);

		// What goes: any [men] minion in play, the old bearer and the new among them, not the [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, warrior, saruman, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hillsman);

		assertInZone(Zone.STACKED, hillsman);
		assertTrue(scn.GetStackedCards(armor).contains(hillsman));
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertAttachedTo(brand, warrior);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherMenMinionWinningOrTheBearerLosingIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetShadowCard("brand");
		var haradrim = scn.GetShadowCard("haradrim");
		var warrior = scn.GetShadowCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim, warrior);
		scn.AttachCardsTo(warrior, brand);
		assertAttachedTo(brand, warrior);
		// Boromir outfights the bearer (7 + 4 against 8 + 2), who lives through the loss with a wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, haradrim},
				new PhysicalCardImpl[]{boromir, warrior});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// The Detachment, a [men] minion not bearing it, beat Aragorn.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brand));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(10, scn.GetStrength(warrior));
		assertEquals(11, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(warrior));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brand));
		assertAttachedTo(brand, warrior);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherMenMinionThatMayBearItTheWinIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetShadowCard("brand");
		var hillsman = scn.GetShadowCard("hillsman");
		var savage = scn.GetShadowCard("savage");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var brigand = scn.GetShadowCard("brigand");
		var armor = scn.GetShadowCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(hillsman, savage, brigand);
		scn.AttachCardsTo(hillsman, brand);
		scn.AttachCardsTo(savage, poleaxe);
		scn.MoveCardsToSupportArea(armor);
		assertAttachedTo(brand, hillsman);
		assertAttachedTo(poleaxe, savage);
		assertInZone(Zone.SUPPORT, armor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillsman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Hillsman (7 + 2) beat Aragorn, but the only other minions are a [dunland] Man and a [men] Man who
		// bears a hand weapon already: there is nobody to transfer it to.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brand));
		assertAttachedTo(brand, hillsman);
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeTheBearerWinsItIsOfferedAgainAndDecliningLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetShadowCard("brand");
		var brand2 = scn.GetShadowCard("brand2");
		var haradrim = scn.GetShadowCard("haradrim");
		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetShadowCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		// The Savage stays out of the skirmishes: a [men] minion to take the Brand at the second win.
		// The Detachment, in no skirmish, bears a second copy, which answers only its own bearer.
		scn.MoveMinionsToTable(hillsman, warrior, savage, haradrim);
		scn.AttachCardsTo(hillsman, brand);
		scn.AttachCardsTo(haradrim, brand2);
		assertAttachedTo(brand2, haradrim);
		scn.MoveCardsToSupportArea(armor);
		assertAttachedTo(brand, hillsman);
		assertInZone(Zone.SUPPORT, armor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hillsman},
				new PhysicalCardImpl[]{boromir, warrior});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brand));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brand2));
		scn.ShadowAcceptOptionalTrigger();
		// The Detachment bears the second copy, a hand weapon: he may not take this one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(haradrim));
		scn.ShadowChooseCard(warrior);
		assertAttachedTo(brand, warrior);
		// The Armor is the one place, taken unasked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, warrior, savage));
		scn.ShadowChooseCard(hillsman);
		assertTrue(scn.GetStackedCards(armor).contains(hillsman));

		scn.FreepsResolveSkirmish(boromir);
		// 8 + 2 for the Brand, against Boromir's 7.
		assertEquals(10, scn.GetStrength(warrior));
		scn.PassCurrentPhaseActions();

		// The new bearer's win is answered as well.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brand));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brand2));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(brand, warrior);
		assertAttachedTo(brand2, haradrim);
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMenPossessionInTheSupportAreaItIsTransferredAndNothingIsStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetShadowCard("brand");
		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var hides = scn.GetShadowCard("hides");
		var companies = scn.GetShadowCard("companies");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(hillsman, warrior);
		scn.AttachCardsTo(hillsman, brand);
		scn.MoveCardsToSupportArea(hides, companies);
		assertAttachedTo(brand, hillsman);
		assertInZone(Zone.SUPPORT, hides, companies);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillsman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brand));
		scn.ShadowAcceptOptionalTrigger();

		// Transferred, and with no [men] possession in the support area nothing is stacked.
		assertAttachedTo(brand, warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, hillsman, warrior);
		assertEquals(0, scn.GetStackedCards(hides).size());
		assertEquals(0, scn.GetStackedCards(companies).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
