package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("numenorean", "8_49");
					put("numenorean2", "8_49");
					// Ships of Great Draught: [raider] support-area possession that takes [raider] tokens by its own
					// Shadow ability; its abilities are used only on purpose
					put("ships", "8_65");

					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability used
					// only on purpose
					put("boromir", "1_97");
					// Narsil, Blade of the Faithful: [gondor] artifact hand weapon on a [gondor] Man, vitality +1; its
					// response is to a wound, and none is placed while it is borne
					put("narsil", "9_34");
					// Armor: [gondor] armor possession on a Man; no more than 1 wound in each skirmish phase
					put("armor", "1_92");
					put("armor2", "1_92");
					// Elven Cloak: [elven] cloak possession on a companion; minion archery total -1
					put("cloak", "1_42");
					// Spider Poison: a [gollum] Shadow possession a companion can bear; each time its bearer is assigned
					// to a skirmish he exerts
					put("poison", "8_28");
					// Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					put("knight", "5_35");
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally whose maneuver ability lets him skirmish
					put("thrarin", "1_27");
					// Dwarven Axe: [dwarven] hand weapon possession on a Dwarf; its trigger needs the bearer to win
					put("axe", "1_9");
					// A Path Appointed: [gondor] support-area condition that takes [gondor] tokens; its abilities are
					// used only on purpose
					put("path", "8_42");

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "5_35");
					put("card2", "5_35");
					put("card3", "5_35");
					put("card4", "5_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackNumenoreanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Black Númenorean
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>. <b>Fierce</b>.<br><b>Skirmish:</b> If you have initiative, remove a [raider] token to discard a possession borne by a character skirmishing this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("numenorean");

		assertEquals("Black Númenorean", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovesARaiderTokenToDiscardAPossessionBorneByTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var narsil = scn.GetFreepsCard("narsil");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var cloak = scn.GetFreepsCard("cloak");
		var knight = scn.GetFreepsCard("knight");
		var numenorean = scn.GetShadowCard("numenorean");
		var numenorean2 = scn.GetShadowCard("numenorean2");
		var ships = scn.GetShadowCard("ships");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(boromir, knight);
		scn.AttachCardsTo(boromir, narsil, armor, cloak);
		scn.AttachCardsTo(knight, armor2);
		// Exactly 3 cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AttachCardsTo(boromir, poison);
		scn.AddTokensToCard(ships, 2);
		scn.MoveMinionsToTable(numenorean, numenorean2);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(cloak, boromir);
		assertAttachedTo(poison, boromir);
		assertAttachedTo(armor2, knight);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, numenorean}, new PhysicalCardImpl[]{knight, numenorean2});
		assertFalse(scn.FreepsHasInitiative());

		// The second copy's skirmish, Boromir waiting for his own against this one: the ability is offered and paid
		// for, and nothing is discarded: the Knight's Armor is borne by a character skirmishing another minion, and
		// Boromir's skirmish against this minion has not begun.
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(numenorean));
		scn.ShadowUseCardAction(numenorean);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertAttachedTo(armor2, knight);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(cloak, boromir);
		assertAttachedTo(poison, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// His own skirmish, against Boromir.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(numenorean));
		scn.ShadowUseCardAction(numenorean);
		assertEquals(0, scn.GetCultureTokensOn(ships));
		// Possessions of two cultures and classes, and a Shadow possession; not the artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, cloak, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(cloak);

		assertInDiscard(cloak);
		assertAttachedTo(narsil, boromir);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(poison, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardsAPossessionBorneByAnAllySkirmishingHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var axe = scn.GetFreepsCard("axe");
		var numenorean = scn.GetShadowCard("numenorean");
		var ships = scn.GetShadowCard("ships");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, axe);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		scn.MoveMinionsToTable(numenorean);
		assertAttachedTo(axe, thrarin);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin's own ability lets him skirmish.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, numenorean);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(numenorean));
		scn.ShadowUseCardAction(numenorean);
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheFreePeoplesPlayerHasFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var numenorean = scn.GetShadowCard("numenorean");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, armor);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"));
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		scn.MoveMinionsToTable(numenorean);
		assertAttachedTo(armor, boromir);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, numenorean);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.FreepsHasInitiative());

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(numenorean));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertAttachedTo(armor, boromir);
	}

	@Test
	public void IsNotOfferedWithNoRaiderTokenAndATokenOfAnotherCultureInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var path = scn.GetFreepsCard("path");
		var numenorean = scn.GetShadowCard("numenorean");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, armor);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));
		scn.MoveCardsToSupportArea(path, ships);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.MoveMinionsToTable(numenorean);
		assertAttachedTo(armor, boromir);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, numenorean);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertFalse(scn.FreepsHasInitiative());

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(numenorean));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertAttachedTo(armor, boromir);
	}
}
