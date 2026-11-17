package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "15_9");
					put("armor2", "15_9");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6, vitality 3, damage +1; a Skirmish ability used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5 (+2 while skirmishing an Orc), vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a Fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an Archery ability only
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4; his text needs a weapon
					put("merry", "1_303");

					// The cards that carry tokens, each given tokens of its own culture by its own text (cheated in here).
					// Suspended Palaces ([dwarven] condition) and Strands of Elven Hair ([elven] possession) have Fellowship
					// abilities only; Stout and Strong ([dwarven] condition, a token each time a Dwarf wins a skirmish) a
					// Skirmish ability used only on purpose.
					put("palaces", "6_10");
					put("stout", "4_57");
					put("strands", "9_22");

					// [dwarven] cards for the hand, one of each kind: a companion, a possession and an event.
					put("farin2", "1_11");
					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon
					put("axestrike", "1_3");    // Axe Strike: [dwarven] skirmish event
					put("lorien", "1_53");      // Lórien Elf: an [elven] companion, the card of another culture

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a Regroup ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WellcraftedArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Well-crafted Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Dwarf. Each time bearer takes a wound, you may reinforce a [dwarven] token.<br><b>Skirmish:</b> Discard a [dwarven] card from hand to make bearer <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Well-crafted Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarfCompanionOrADwarfAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		assertInHand(armor);
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(armor, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerTakesAWoundADwarvenTokenMayBeReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var palaces = scn.GetFreepsCard("palaces");
		var stout = scn.GetFreepsCard("stout");
		var strands = scn.GetFreepsCard("strands");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, armor);
		// A second copy, on another Dwarf who is not wounded.
		scn.AttachCardsTo(farin, armor2);
		scn.MoveCardsToSupportArea(palaces, stout, strands);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 1);
		scn.AddTokensToCard(stout, 1);
		scn.AddTokensToCard(strands, 2);
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetCultureTokensOn(strands));

		scn.MoveMinionsToTable(servant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, servant);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.PassSkirmishActions();

		// 6 against 7: Gimli loses and takes a wound.  Only the copy he bears answers it.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor2));
		scn.FreepsChooseOptionalTrigger(armor);

		// Either [dwarven] card that has a token; not the [elven] one, nor a [dwarven] card with no token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(palaces, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strands, armor, armor2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stout);

		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundOnAnotherDwarfWhoDoesNotBearItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		var palaces = scn.GetFreepsCard("palaces");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(palaces);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 1);
		assertEquals(1, scn.GetCultureTokensOn(palaces));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, troop);
		scn.FreepsResolveSkirmish(farin);
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.PassSkirmishActions();

		// 7 against 13: Farin loses and takes a wound; Gimli, the bearer, takes none.
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoDwarvenTokenInPlayTheWoundOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var palaces = scn.GetFreepsCard("palaces");
		var strands = scn.GetFreepsCard("strands");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		// Suspended Palaces with its tokens all spent, beside two [elven] tokens.
		scn.MoveCardsToSupportArea(palaces, strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetCultureTokensOn(strands));

		scn.MoveMinionsToTable(servant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, servant);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The offer would reinforce nothing, and is not made.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoWoundsFromOneLostSkirmishOfferTheReinforcementTwiceAndADeclineAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var palaces = scn.GetFreepsCard("palaces");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(palaces);

		scn.StartGame();
		scn.AddTokensToCard(palaces, 1);
		assertEquals(1, scn.GetCultureTokensOn(palaces));

		scn.MoveMinionsToTable(sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, sentry);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();

		// 6 against 8, and the Sentry's damage +1: two wounds, each a wound taken.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsADwarvenCardFromHandToMakeBearerDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var merry = scn.GetFreepsCard("merry");
		var armor = scn.GetFreepsCard("armor");
		var farin2 = scn.GetFreepsCard("farin2");
		var axe = scn.GetFreepsCard("axe");
		var axestrike = scn.GetFreepsCard("axestrike");
		var lorien = scn.GetFreepsCard("lorien");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, merry);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(farin2, axe, axestrike, lorien);

		scn.StartGame();
		assertInHand(farin2, axe, axestrike, lorien);
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{merry, troop});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(armor));
		scn.FreepsUseCardAction(armor);

		// A [dwarven] card of each kind may pay; the [elven] one may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin2, axe, axestrike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorien));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axestrike);

		assertInDiscard(axestrike);
		assertInHand(farin2, axe, lorien);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Each skirmish is a phase of its own: in Merry's, Gimli is back to his printed damage +1.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void SkirmishAbilityReachesBearerInNoSkirmishAndAddsUpEachTimeItIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		var axestrike = scn.GetFreepsCard("axestrike");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, merry);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(axe, axestrike);

		scn.StartGame();
		assertInHand(axe, axestrike);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		// Gimli is assigned to nobody.
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(armor));
		scn.FreepsUseCardAction(armor);
		scn.FreepsChooseCard(axestrike);
		assertInDiscard(axestrike);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(armor));
		scn.FreepsUseCardAction(armor);
		assertInDiscard(axe);
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoDwarvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var lorien = scn.GetFreepsCard("lorien");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(lorien);

		scn.StartGame();
		assertInHand(lorien);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Gimli's own ability is offered; the Armor's cannot be paid.
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(armor));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}
}
