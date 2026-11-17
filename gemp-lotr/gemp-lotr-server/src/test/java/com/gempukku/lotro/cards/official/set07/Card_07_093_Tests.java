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

public class Card_07_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_93");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3, no other text
					put("kog", "5_36");         // Knight of Gondor: [gondor] Man companion, strength 5; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, no Man; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant, no other text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; an ability that needs initiative
					put("narsil", "9_34");      // Narsil: a [gondor] artifact, here only a card in hand
					put("numenor", "5_37");     // Men of Númenor: a [gondor] skirmish event, here only a card in hand
					put("wall", "5_33");        // City Wall: a [gondor] fortification, strength -3 on a minion
					put("wall2", "5_33");
					put("parapet", "5_87");     // Parapet: a [rohan] fortification; an archery ability only
					put("rally", "5_38");       // Rally Point: a [gondor] condition that is no fortification; a regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOverwhelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_93");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, no text, not unique
					put("brigand2", "4_10");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					// The Free Peoples hand: two companions, which offer nothing in a skirmish.
					put("lorien", "1_53");      // Lórien Elf
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FootmansArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Footman's Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Response:</b> If bearer is about to be overwhelmed, discard a [gondor] fortification or a card from hand to make bearer strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Footman's Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseDiscardsACardFromHandToMakeTheBearerStrengthPlusTwoWhenHeIsAboutToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(lorien, guard);

		scn.StartGame();
		assertAttachedTo(armor, aragorn);
		assertInZone(Zone.HAND, lorien, guard);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		// The Free Peoples player may give him one minion; the Shadow player gives him the one left over.
		scn.FreepsAssignToMinions(aragorn, brigand1);
		scn.ShadowAssignToMinions(aragorn, brigand2);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 + 8 against 8: exactly double.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		scn.FreepsChooseOptionalTrigger(armor);
		// No [gondor] fortification is in play, so the card comes from hand: either of the two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.HAND, lorien);
		// 16 against 10 is no longer double: he loses to two minions with no damage bonus, takes one wound and
		// lives.  He is not about to be overwhelmed any more, so the response is not offered again, though a card
		// is still in hand to pay for it.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(armor, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsOfferedAgainWhileTheBearerIsStillAboutToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var brigand1 = scn.GetShadowCard("brigand1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(lorien, guard);

		scn.StartGame();
		assertAttachedTo(armor, aragorn);
		assertInZone(Zone.HAND, lorien, guard);
		scn.MoveMinionsToTable(brigand1, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand1);
		scn.ShadowAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 + 13 against 8.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		scn.FreepsChooseOptionalTrigger(armor);
		scn.FreepsChooseCard(guard);
		// 21 against 10 is still more than double: whether he is about to be overwhelmed is checked again, and the
		// response is offered again (CR 4.2, the ruling on Footman's Armor).
		assertEquals(10, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.HAND, lorien);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		// The one card left in hand pays, with nothing to choose.  21 against 12 is no longer double: he loses,
		// takes one wound and lives.
		scn.FreepsChooseOptionalTrigger(armor);
		assertInZone(Zone.DISCARD, lorien);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerMustBeAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		assertInZone(Zone.HAND, armor);
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);
		// A [gondor] Man, companion or ally; not Pippin ([gondor], no Man), the Household Guard (a Man, [rohan]) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(armor, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCardInHandAGondorFortificationInPlayPaysAndTheBonusLastsTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var wall2 = scn.GetFreepsCard("wall2");
		var parapet = scn.GetFreepsCard("parapet");
		var rally = scn.GetFreepsCard("rally");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(knight, armor);
		scn.MoveCardsToSupportArea(wall, parapet, rally);

		scn.StartGame();
		assertAttachedTo(armor, knight);
		assertInZone(Zone.SUPPORT, wall, parapet, rally);
		scn.MoveMinionsToTable(troop, brigand);
		// The second City Wall is a [gondor] fortification on a minion: still one the player can discard.
		scn.AttachCardsTo(brigand, wall2);
		assertAttachedTo(wall2, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{aragorn, brigand});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetFreepsHandCount());
		assertAttachedTo(armor, knight);
		assertAttachedTo(wall2, brigand);
		assertInZone(Zone.SUPPORT, wall, parapet, rally);
		// 13 against 6: at least double.
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		// Only the fortification half can be paid, so no choice of halves is asked.
		scn.FreepsChooseOptionalTrigger(armor);
		// Either City Wall, in the support area or on a minion; not Parapet ([rohan]) or Rally Point (no fortification).
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall, wall2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(parapet, rally));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wall2);

		assertInZone(Zone.DISCARD, wall2);
		assertInZone(Zone.SUPPORT, wall, parapet, rally);
		// 13 against 6 + 2 is no longer double: he loses, takes one wound and lives.
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertEquals(1, scn.GetWoundsOn(knight));

		// The next skirmish is a phase of its own: the +2 is gone. The Brigand has his 8 back.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithBothHalvesPayableThePlayerIsAskedAndAnyCardFromHandPays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var narsil = scn.GetFreepsCard("narsil");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, armor);
		scn.MoveCardsToSupportArea(wall);
		scn.MoveCardsToHand(guard, narsil, numenor);

		scn.StartGame();
		assertAttachedTo(armor, knight);
		assertInZone(Zone.HAND, guard, narsil, numenor);
		assertInZone(Zone.SUPPORT, wall);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		assertInZone(Zone.HAND, guard, narsil, numenor);
		assertInZone(Zone.SUPPORT, wall);
		assertEquals(6, scn.GetStrength(knight));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		scn.FreepsChooseOptionalTrigger(armor);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("fortification"));
		assertTrue(scn.FreepsChoiceAvailable("hand"));
		scn.FreepsChooseOption("hand");
		// Any card: a companion, an artifact and an event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, narsil, numenor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(numenor);

		assertInZone(Zone.DISCARD, numenor);
		assertInZone(Zone.HAND, guard, narsil);
		assertInZone(Zone.SUPPORT, wall);
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		// The skirmish phase is over, and the +2 with it.
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoCardInHandAndNoGondorFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var parapet = scn.GetFreepsCard("parapet");
		var rally = scn.GetFreepsCard("rally");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, armor);
		scn.MoveCardsToSupportArea(parapet, rally);

		scn.StartGame();
		assertAttachedTo(armor, knight);
		assertInZone(Zone.SUPPORT, parapet, rally);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetFreepsHandCount());
		assertAttachedTo(armor, knight);
		assertInZone(Zone.SUPPORT, parapet, rally);
		assertEquals(6, scn.GetStrength(knight));
		scn.PassSkirmishActions();

		// Nothing to pay with: he is overwhelmed and killed.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertInZone(Zone.DEAD, knight);
		assertInZone(Zone.SUPPORT, parapet, rally);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesWithoutBeingOverwhelmedNorWhenAnotherCompanionIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight, kog);
		scn.AttachCardsTo(knight, armor);
		scn.MoveCardsToHand(guard);

		scn.StartGame();
		assertAttachedTo(armor, knight);
		assertInZone(Zone.HAND, guard);
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, brigand}, new PhysicalCardImpl[]{kog, troop});

		// 8 against 6: the bearer loses, but is not overwhelmed.
		scn.FreepsResolveSkirmish(knight);
		assertInZone(Zone.HAND, guard);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInZone(Zone.HAND, guard);

		// 13 against 5: the Knight of Gondor is overwhelmed, but he does not bear the Armor.
		scn.FreepsResolveSkirmish(kog);
		assertEquals(5, scn.GetStrength(kog));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertInZone(Zone.DEAD, kog);
		assertInZone(Zone.HAND, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
