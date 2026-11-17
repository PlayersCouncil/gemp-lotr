package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_245_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_245");
					// Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					put("hguard", "5_83");
					put("hguard2", "5_83");
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his abilities are used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, no Man beside fewer than 3 [rohan] companions
					// and no Eowyn; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text

					// Cards held in hand, of four types: a companion (Gondorian Knight), a possession (Hobbit Sword),
					// an event (Sorry About Everything) and a condition (A Talent for Not Being Seen)
					put("h1", "5_35");
					put("h2", "1_299");
					put("h3", "1_312");
					put("h4", "1_316");

					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					// Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time he wins a skirmish, he may exert
					// twice to wound a companion
					put("skirmisher", "18_90");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidingArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Riding Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a [rohan] Man.<br><b>Response:</b> If you have initiative and bearer is about to take a wound in a skirmish, discard 3 cards from hand to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Riding Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndNotOnARohanHobbitOrAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var hguard = scn.GetFreepsCard("hguard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(hguard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		assertInHand(armor);
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertAttachedTo(armor, guma);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithInitiativeThreeCardsFromHandPreventAWoundAndTheNextWoundFindsNoInitiativeLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var hguard = scn.GetFreepsCard("hguard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		scn.MoveCompanionsToTable(hguard);
		scn.AttachCardsTo(hguard, armor);
		scn.MoveCardsToHand(h1, h2, h3, h4);

		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		scn.MoveMinionsToTable(cuguard);
		assertAttachedTo(armor, hguard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(h1, h2, h3, h4);
		assertEquals(0, scn.GetWoundsOn(hguard));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard, cuguard);
		scn.FreepsResolveSkirmish(hguard);

		// 9 against 6: the Uruk-hai wins, and its damage +1 makes two wounds.
		scn.PassSkirmishActions();

		// Four cards in hand: the Free Peoples player has initiative.
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsChooseOptionalTrigger(armor);

		// Any three cards: a companion, a possession, an event and a condition are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(h1, h2, h3, h4));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(h1, h3, h4);

		assertInDiscard(h1, h3, h4);
		assertInHand(h2);

		// The second wound comes with one card left in hand: the Shadow has initiative, and it is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertInHand(h2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var hguard = scn.GetFreepsCard("hguard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		scn.MoveCompanionsToTable(hguard);
		scn.AttachCardsTo(hguard, armor);
		scn.MoveCardsToHand(h1, h2, h3);

		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		scn.MoveMinionsToTable(cuguard);
		assertAttachedTo(armor, hguard);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(hguard));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard, cuguard);
		scn.FreepsResolveSkirmish(hguard);

		scn.PassSkirmishActions();

		// Three cards could be discarded, but with three in hand the Shadow has initiative.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(hguard));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundOnAnotherRohanManWhoDoesNotBearItBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var hguard = scn.GetFreepsCard("hguard");
		var hguard2 = scn.GetFreepsCard("hguard2");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		scn.MoveCompanionsToTable(hguard, hguard2);
		scn.AttachCardsTo(hguard, armor);
		scn.MoveCardsToHand(h1, h2, h3, h4);

		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		scn.MoveMinionsToTable(cuguard);
		assertAttachedTo(armor, hguard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(hguard2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard2, cuguard);
		scn.FreepsResolveSkirmish(hguard2);

		// 9 against 6: the second Guard, who bears no armor, takes two wounds; the bearer none.
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(hguard2));
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsAWoundTheBearerTakesInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var hguard = scn.GetFreepsCard("hguard");
		var gknight = scn.GetFreepsCard("gknight");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		scn.MoveCompanionsToTable(hguard, gknight);
		scn.AttachCardsTo(hguard, armor);
		scn.MoveCardsToHand(h1, h2, h3, h4);

		var skirmisher = scn.GetShadowCard("skirmisher");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, troop);
		assertAttachedTo(armor, hguard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(h1, h2, h3, h4);
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(gknight));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hguard, troop}, new PhysicalCardImpl[]{gknight, skirmisher});

		// The Skirmisher, 9, beats the Gondorian Knight, 6, and exerts twice to wound the bearer, whose own skirmish
		// is still to come: a wound taken during a skirmish.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(hguard);

		assertEquals(0, scn.GetWoundsOn(hguard));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsChooseOptionalTrigger(armor);
		scn.FreepsChooseCards(h1, h2, h3);

		assertInDiscard(h1, h2, h3);
		assertInHand(h4);
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotOfferedForAnArcheryWoundOnTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var hguard = scn.GetFreepsCard("hguard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		scn.MoveCompanionsToTable(hguard);
		scn.AttachCardsTo(hguard, armor);
		scn.MoveCardsToHand(h1, h2, h3, h4);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		assertAttachedTo(armor, hguard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(hguard));
		scn.SkipToArcheryWounds();

		// The Marksman's archery wound, placed by the Free Peoples player on the bearer, with initiative.
		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsChooseCard(hguard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
