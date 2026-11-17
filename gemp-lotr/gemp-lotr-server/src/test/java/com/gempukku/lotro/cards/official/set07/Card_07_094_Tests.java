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

public class Card_07_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "7_94");
					put("bow2", "7_94");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man knight, strength 6, vitality 3, no other text
					put("kog", "5_36");         // Knight of Gondor: [gondor] Man knight, strength 5; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, no knight, strength 8; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, no Man; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant, no other text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; an ability that needs initiative
					put("narsil", "9_34");      // Narsil: vitality +1, so the Merchant can exert; a response to its bearer's wound only
					put("seventh", "7_119");    // Seventh Level: a fortification, strength -2 and vitality -2 on a minion
					put("wall", "5_33");        // City Wall: a fortification, strength -3 on a minion
					put("wall2", "5_33");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2; his bonus needs 2 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("axe", "4_26");         // Iron Axe: a [dunland] hand weapon for the Brigand, no text of its own: a possession that is no fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondor Bow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be a [gondor] Man.<br>If bearer is a knight, he or she is strength +1.<br><b>Skirmish:</b> Exert bearer to wound a minion he or she is skirmishing if that minion bears a fortification.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Gondor Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnAGondorManAndAKnightBearerIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight, kog, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(bow, bow2);

		scn.StartGame();

		assertInZone(Zone.HAND, bow, bow2);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(5, scn.GetStrength(kog));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(bow);
		// A [gondor] Man, companion or ally; not Pippin ([gondor], no Man), the Household Guard (a Man, [rohan]) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, kog, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		// A knight bearing it: 6 + 1. The Knight of Gondor, a knight who does not bear it, is his printed 5.
		assertAttachedTo(bow, knight);
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(5, scn.GetStrength(kog));

		// Aragorn bears the second: he is no knight, and stays 8.
		scn.FreepsPlayCard(bow2);
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(bow2, aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(5, scn.GetStrength(kog));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBearerToWoundAMinionHeIsSkirmishingThatBearsAFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var seventh = scn.GetFreepsCard("seventh");
		var wall = scn.GetFreepsCard("wall");
		var wall2 = scn.GetFreepsCard("wall2");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(knight, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2, axeman);
		scn.AttachCardsTo(troop, seventh);
		scn.AttachCardsTo(axeman, wall);
		scn.AttachCardsTo(troop2, wall2);
		assertAttachedTo(bow, knight);
		assertAttachedTo(seventh, troop);
		assertAttachedTo(wall, axeman);
		assertAttachedTo(wall2, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{aragorn, troop2});
		scn.ShadowAssignToMinions(knight, axeman);
		scn.FreepsResolveSkirmish(knight);

		// The Troop with Seventh Level has vitality 3 - 2 = 1: a wound kills it.
		assertEquals(1, scn.GetVitality(troop));
		assertEquals(2, scn.GetVitality(axeman));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(knight));
		// Either minion he is skirmishing, each bearing a fortification; not the second Troop, which bears one but
		// is assigned to Aragorn, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, axeman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertInZone(Zone.DISCARD, troop);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsUseCardAction(bow);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionHeIsSkirmishingWithNoFortificationIsNotWoundedThoughTheExertionIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var axe = scn.GetShadowCard("axe");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(knight, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, axeman);
		// The Brigand bears a possession that is no fortification.
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(axeman, wall);
		assertAttachedTo(bow, knight);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(wall, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, brigand}, new PhysicalCardImpl[]{aragorn, axeman});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsUseCardAction(bow);

		// The Brigand bears no fortification (his vitality of 1 would show a wound); the Axeman bears one but is in
		// Aragorn's skirmish, still to come.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyBearerExertsAndWoundsNobodyInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var wall = scn.GetFreepsCard("wall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, wall);
		assertAttachedTo(narsil, merchant);
		assertAttachedTo(bow, merchant);
		assertAttachedTo(wall, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		// The Merchant is in no skirmish; the Troop, bearing City Wall, fights the Gondorian Knight.
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsUseCardAction(bow);

		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var wall = scn.GetFreepsCard("wall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, bow);

		scn.StartGame();
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, wall);
		assertAttachedTo(bow, knight);
		assertAttachedTo(wall, troop);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}
}
