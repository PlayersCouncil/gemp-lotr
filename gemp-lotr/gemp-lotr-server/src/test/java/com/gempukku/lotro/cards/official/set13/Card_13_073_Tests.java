package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kingsfoil", "13_73");
					put("kingsfoil2", "13_73");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("merchant", "7_97");     // Gondorian Merchant: the [gondor] Man ally; his ability needs initiative
					put("pippin", "103_31");     // Pippin, Trollslayer: a [gondor] Hobbit companion (no [shire]); his skirmish ability is used only on purpose
					put("rider", "4_286");       // Rider of Rohan: a [rohan] Man companion; his ability wants a mount
					put("merry", "1_302");       // Merry, Friend to Sam: a [shire] Hobbit companion; a skirmish ability used only on purpose
					put("smeagol", "103_116");   // Sméagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("barliman", "1_70");     // Barliman Butterbur: a Man ally, vitality 2; a fellowship ability used only on purpose
					put("draught", "9_24");      // Ent Draught: a Free Peoples condition a companion bears; a skirmish ability used only on purpose
					put("pipe", "1_292");        // The Gaffer's Pipe: a possession a Hobbit bears; its ability wants a pipeweed
					put("lost1", "11_212");      // Lost in the Woods: a Shadow condition a companion bears; strength only for [wraith] minions skirmishing him
					put("lost2", "11_212");
					put("savage", "1_151");      // Uruk Savage: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsfoilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Kingsfoil
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Fellowship:</b> Discard this possession from play to heal a companion and, if that companion is a [shire] companion, you may discard a condition he or she bears.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kingsfoil");

		assertEquals("Kingsfoil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAGondorManCompanionOrAllyOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(kingsfoil);

		scn.StartGame();
		assertInHand(kingsfoil);
		assertTrue(scn.FreepsPlayAvailable(kingsfoil));
		scn.FreepsPlayCard(kingsfoil);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		// A [gondor] Hobbit and a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(kingsfoil, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedToHealACompanionAndAShireCompanionMayLoseAConditionHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var barliman = scn.GetFreepsCard("barliman");
		var draught = scn.GetFreepsCard("draught");
		var pipe = scn.GetFreepsCard("pipe");
		var lost1 = scn.GetShadowCard("lost1");
		var lost2 = scn.GetShadowCard("lost2");
		var merchant = scn.GetFreepsCard("merchant");
		var kingsfoil2 = scn.GetFreepsCard("kingsfoil2");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(barliman, merchant);
		scn.AttachCardsTo(aragorn, kingsfoil, lost2);
		// A second copy, which stays where it is.
		scn.AttachCardsTo(merchant, kingsfoil2);
		scn.AttachCardsTo(merry, lost1, draught, pipe);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertAttachedTo(kingsfoil, aragorn);
		assertAttachedTo(kingsfoil2, merchant);
		assertAttachedTo(lost2, aragorn);
		assertAttachedTo(lost1, merry);
		assertAttachedTo(draught, merry);
		assertAttachedTo(pipe, merry);

		assertTrue(scn.FreepsActionAvailable(kingsfoil));
		scn.FreepsUseCardAction(kingsfoil);
		assertInDiscard(kingsfoil);
		// Wounded companions of two cultures; a wounded ally is not one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Merry is a [shire] companion: a Shadow or a Free Peoples condition he bears, and not his possession
		// or a condition another companion bears.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lost1, draught));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, lost2));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(lost1);

		assertInDiscard(lost1);
		assertAttachedTo(draught, merry);
		assertAttachedTo(pipe, merry);
		assertAttachedTo(lost2, aragorn);
		assertAttachedTo(kingsfoil2, merchant);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireCompanionWhoIsNoHobbitIsOfferedTheDiscardAndMayDeclineIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var lost1 = scn.GetShadowCard("lost1");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.AttachCardsTo(aragorn, kingsfoil);
		scn.AttachCardsTo(smeagol, lost1);
		scn.AddWoundsToChar(smeagol, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertAttachedTo(lost1, smeagol);
		assertAttachedTo(kingsfoil, aragorn);

		scn.FreepsUseCardAction(kingsfoil);
		// Sméagol is the one wounded companion: healed with nothing asked.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lost1));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertAttachedTo(lost1, smeagol);
		assertInDiscard(kingsfoil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorHobbitHealedIsNotOfferedTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var lost1 = scn.GetShadowCard("lost1");
		var lost2 = scn.GetShadowCard("lost2");
		scn.MoveCompanionsToTable(aragorn, pippin, merry);
		scn.AttachCardsTo(aragorn, kingsfoil);
		scn.AttachCardsTo(pippin, lost1);
		// A [shire] companion bearing a condition beside him.
		scn.AttachCardsTo(merry, lost2);
		scn.AddWoundsToChar(pippin, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertAttachedTo(lost1, pippin);
		assertAttachedTo(lost2, merry);
		assertAttachedTo(kingsfoil, aragorn);

		scn.FreepsUseCardAction(kingsfoil);
		// Pippin is the one wounded companion: healed with nothing asked, and he is no [shire] companion.
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertAttachedTo(lost1, pippin);
		assertAttachedTo(lost2, merry);
		assertInDiscard(kingsfoil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireCompanionWhoBearsNoConditionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var lost1 = scn.GetShadowCard("lost1");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, kingsfoil, lost1);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertAttachedTo(lost1, aragorn);
		assertAttachedTo(kingsfoil, aragorn);

		scn.FreepsUseCardAction(kingsfoil);
		assertEquals(0, scn.GetWoundsOn(merry));
		// Nothing of his to discard: no question, and Aragorn's condition is not his.
		assertAttachedTo(lost1, aragorn);
		assertInDiscard(kingsfoil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCompanionWoundedItIsStillUsedAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(aragorn, kingsfoil);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertAttachedTo(kingsfoil, aragorn);

		assertTrue(scn.FreepsActionAvailable(kingsfoil));
		scn.FreepsUseCardAction(kingsfoil);
		assertInDiscard(kingsfoil);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, kingsfoil);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(kingsfoil));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(kingsfoil));
		assertAttachedTo(kingsfoil, aragorn);
	}
}
