package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_230_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "1_230");
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] hand weapon on a [moria] Orc; a play trigger only
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: a hand weapon (possession) on Aragorn, damage +1
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an artifact hand weapon on a [gondor] Man; its response is used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: a ranged weapon on Aragorn; makes him an archer
					put("mail", "1_101");       // Coat of Mail: armor, a possession that is no weapon
					put("merry", "1_302");      // Merry, Friend to Sam: a skirmish ability used only on purpose
					put("hobbitsword", "1_299"); // Hobbit Sword: a hand weapon on a Hobbit, no other text
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, home 1F
					put("follow", "1_109");     // One Whom Men Would Follow: exert Aragorn to have an ally take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Cantëa, Lieutenant of Dol Guldur
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.
		 * <b>Skirmish:</b> Exert Úlairë Cantëa to discard a weapon borne by a character he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Dol Guldur", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishActionExertsHimToDiscardAWeaponBorneByTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		var mail = scn.GetFreepsCard("mail");
		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, narsil, mail);
		scn.AttachCardsTo(merry, hobbitsword);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, scout, runner);
		scn.AttachCardsTo(scout, scimitar);

		scn.SkipToAssignments();
		// Merry's skirmish has not begun when Cantea acts.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cantea}, new PhysicalCardImpl[]{merry, runner});
		// The Scout is a second minion in Aragorn's skirmish, bearing a weapon of his own.
		scn.ShadowAssignToMinions(aragorn, scout);
		// The bow makes Aragorn an archer: laid after the archery phase.
		scn.AttachCardsTo(aragorn, bow);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(narsil, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mail, hobbitsword, scimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(narsil);

		assertInDiscard(narsil);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(mail, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(scimitar, scout);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AWeaponBorneByAnAllyHeIsSkirmishingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.MoveCardsToHand(follow);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);

		// Set-up: Maggot takes part in skirmishes until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(maggot, cantea);
		scn.FreepsResolveSkirmish(maggot);

		assertTrue(scn.IsCharSkirmishingAgainst(maggot, cantea));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertInDiscard(hobbitsword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		// Vitality 3: with 2 wounds he cannot exert.
		scn.AddWoundsToChar(cantea, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
		assertAttachedTo(sword, aragorn);
	}
}
