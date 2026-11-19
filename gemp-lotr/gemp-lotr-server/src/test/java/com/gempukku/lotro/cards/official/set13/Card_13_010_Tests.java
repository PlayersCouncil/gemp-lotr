package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("asfaloth", "13_10");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgûl
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion; +2 only at sites 6 to 8
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally, vitality 4; heals allies of site 3 at the start of his owner's turns
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
					// Shake Off the Shadow: [elven] support-area condition, not unique, that holds [elven] tokens; its
					// abilities are used only on purpose
					put("shake1", "8_13");
					put("shake2", "8_13");
					// Strands of Elven Hair: [elven] support-area possession; tokens only as it is played, so cheated in it has none
					put("strands", "9_22");
					// Garrison of Osgiliath: [gondor] support-area condition that holds [gondor] tokens
					put("garrison", "6_52");
					put("bow", "1_41");         // Elven Bow: [elven] possession an Elf bears; it makes him an archer
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("pursuer1", "4_190");   // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3; its text is on unbound Hobbits
					put("pursuer2", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AsfalothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Asfaloth, Swift Blossom
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Resistance: +1
		 * Game Text: Bearer must be an Elf.<br>When you play Asfaloth, you may reinforce an [elven] token.<br><b>Skirmish:</b> Remove 2 [elven] tokens to heal bearer (or heal bearer and heal another Elf if bearer is in a fierce skirmish).
		*/

		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");

		assertEquals("Asfaloth", asfaloth.getBlueprint().getTitle());
		assertEquals("Swift Blossom", asfaloth.getBlueprint().getSubtitle());
		assertTrue(asfaloth.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, asfaloth.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, asfaloth.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, asfaloth.getBlueprint().getCardType());
		assertTrue(asfaloth.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, asfaloth.getBlueprint().getTwilightCost());
		assertEquals(1, asfaloth.getBlueprint().getResistance());
	}

	@Test
	public void PlayedOnAnElfItMayReinforceAnElvenTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shake1 = scn.GetFreepsCard("shake1");
		var shake2 = scn.GetFreepsCard("shake2");
		var strands = scn.GetFreepsCard("strands");
		var garrison = scn.GetFreepsCard("garrison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(shake1, shake2, strands, garrison);
		scn.AddTokensToCard(shake1, 2);
		scn.AddTokensToCard(shake2, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(2, scn.GetCultureTokensOn(shake1));
		assertEquals(1, scn.GetCultureTokensOn(shake2));
		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.FreepsPlayCard(asfaloth);
		// An Elf companion and an Elf ally may bear it; Aragorn and Frodo are no Elves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);
		assertAttachedTo(asfaloth, elrond);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(asfaloth));
		scn.FreepsAcceptOptionalTrigger();
		// Either [elven] card that has an [elven] token; not the [elven] card with none, nor the card with
		// [gondor] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shake1, shake2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strands, garrison));
		scn.FreepsChooseCard(shake2);
		assertEquals(2, scn.GetCultureTokensOn(shake1));
		assertEquals(2, scn.GetCultureTokensOn(shake2));
		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementMayBeDeclinedAndAnotherPossessionPlayedAfterAsfalothIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var shake1 = scn.GetFreepsCard("shake1");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(shake1);
		scn.AddTokensToCard(shake1, 1);
		scn.MoveCardsToHand(asfaloth, bow);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(shake1));
		// Arwen is the only Elf, so Asfaloth goes on her unasked.
		scn.FreepsPlayCard(asfaloth);
		assertAttachedTo(asfaloth, arwen);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(asfaloth));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(shake1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [elven] possession played after Asfaloth, with the token still there to reinforce.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(asfaloth));
		assertEquals(1, scn.GetCultureTokensOn(shake1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElvenTokenToReinforceTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var strands = scn.GetFreepsCard("strands");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(arwen);
		// An [elven] card with no token, and [gondor] tokens on a [gondor] card.
		scn.MoveCardsToSupportArea(strands, garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.FreepsPlayCard(asfaloth);
		assertAttachedTo(asfaloth, arwen);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(asfaloth));
		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesTwoElvenTokensToHealBearerAndNoOtherElfOutsideAFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var shake1 = scn.GetFreepsCard("shake1");
		var shake2 = scn.GetFreepsCard("shake2");
		var garrison = scn.GetFreepsCard("garrison");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, asfaloth);
		scn.MoveCardsToSupportArea(shake1, shake2, garrison);
		scn.AddTokensToCard(shake1, 1);
		scn.AddTokensToCard(shake2, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(haldir, 1);

		scn.StartGame();
		assertAttachedTo(asfaloth, arwen);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetCultureTokensOn(shake1));
		assertEquals(1, scn.GetCultureTokensOn(shake2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.FreepsActionAvailable(asfaloth));
		scn.FreepsUseCardAction(asfaloth);
		// The tokens come one card at a time: the [elven] cards, never the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shake1, shake2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(shake1);

		assertEquals(0, scn.GetCultureTokensOn(shake1));
		assertEquals(0, scn.GetCultureTokensOn(shake2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		// One wound off bearer; the other Elf is not healed, this being no fierce skirmish.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOnlyOneElvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var shake1 = scn.GetFreepsCard("shake1");
		var garrison = scn.GetFreepsCard("garrison");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, asfaloth);
		scn.MoveCardsToSupportArea(shake1, garrison);
		scn.AddTokensToCard(shake1, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.AddWoundsToChar(arwen, 1);

		scn.StartGame();
		assertAttachedTo(asfaloth, arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetCultureTokensOn(shake1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		// One [elven] token, beside two [gondor] ones, cannot pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(asfaloth));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetCultureTokensOn(shake1));
	}

	@Test
	public void WithBearerUnwoundedTheTokensArePaidAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var shake1 = scn.GetFreepsCard("shake1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, asfaloth);
		scn.MoveCardsToSupportArea(shake1);
		scn.AddTokensToCard(shake1, 2);
		scn.AddWoundsToChar(haldir, 1);

		scn.StartGame();
		assertAttachedTo(asfaloth, arwen);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(2, scn.GetCultureTokensOn(shake1));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(asfaloth));
		scn.FreepsUseCardAction(asfaloth);

		assertEquals(0, scn.GetCultureTokensOn(shake1));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InHerFierceSkirmishBearerAndAnotherElfAreHealedAndInAnotherFierceSkirmishBearerAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shake1 = scn.GetFreepsCard("shake1");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(arwen, asfaloth);
		scn.MoveCardsToSupportArea(shake1);
		scn.AddTokensToCard(shake1, 4);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		// Elrond heals himself at the start of his owner's turn: his wound goes on after it.
		scn.AddWoundsToChar(elrond, 1);
		assertAttachedTo(asfaloth, arwen);
		assertEquals(4, scn.GetCultureTokensOn(shake1));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(pursuer1, pursuer2);

		// The first round: Arwen (6) and Aragorn (8) each beat an Uruk Pursuer (5), which lives to be fierce.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, pursuer1},
				new PhysicalCardImpl[]{aragorn, pursuer2});
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, pursuer1},
				new PhysicalCardImpl[]{aragorn, pursuer2});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(asfaloth));
		scn.FreepsUseCardAction(asfaloth);

		// Bearer is in a fierce skirmish: a wound off her, and another wounded Elf, companion or ally, to heal;
		// not the wounded Aragorn, who is no Elf, nor bearer again.
		assertEquals(2, scn.GetCultureTokensOn(shake1));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, arwen));
		scn.FreepsChooseCard(elrond);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Arwen (6) beats the Pursuer again; then Aragorn's fierce skirmish, with bearer in no skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.FreepsActionAvailable(asfaloth));
		scn.FreepsUseCardAction(asfaloth);

		// Bearer alone is healed: she is not in the fierce skirmish being fought.
		assertEquals(0, scn.GetCultureTokensOn(shake1));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InHerFierceSkirmishWithNoOtherElfWoundedOnlyBearerIsHealedAndTheTokensArePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var elrond = scn.GetFreepsCard("elrond");
		var shake1 = scn.GetFreepsCard("shake1");
		var pursuer1 = scn.GetShadowCard("pursuer1");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(arwen, asfaloth);
		scn.MoveCardsToSupportArea(shake1);
		scn.AddTokensToCard(shake1, 2);
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		assertAttachedTo(asfaloth, arwen);
		assertEquals(2, scn.GetCultureTokensOn(shake1));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.MoveMinionsToTable(pursuer1);

		// Arwen (6) beats the Uruk Pursuer (5), which lives to be fierce.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, pursuer1);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(arwen, pursuer1);
		scn.FreepsResolveSkirmish(arwen);

		// Bearer is in a fierce skirmish but no other Elf is wounded: the ability is offered, the tokens are paid,
		// bearer is healed and there is no one else to choose.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(asfaloth));
		scn.FreepsUseCardAction(asfaloth);

		assertEquals(0, scn.GetCultureTokensOn(shake1));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
