package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tales", "6_75");
					put("tales2", "6_75");      // a second copy: each copy offers for itself
					put("traitor", "6_72");     // Rohirrim Traitor: an [isengard] Man, strength 6, vitality 2; +2 while a companion bears an
					                            // [isengard] condition (none does)
					put("agents", "6_57");      // Agents of Orthanc: an [isengard] Man of another title, strength 9; +2 for each Free Peoples
					                            // culture spotted over 2
					put("guard", "1_147");      // Uruk Guard: an [isengard] Uruk-hai, no Man, strength 9, damage +1; an Assignment ability only
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1; no text
					put("poison", "8_28");      // Spider Poison: a [gollum] Shadow possession that lies in the support area; its text reaches
					                            // a companion skirmishing Shelob, or its own bearer

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6, vitality 3; a Skirmish ability only
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession on a Dwarf; changes no number
					put("ring", "17_4");        // Ring of Artifice: a [dwarven] artifact on a Dwarf; damage +1, and no strength while no
					                            // [dwarven] artifact is in the discard pile
					put("heart", "1_10");       // Dwarven Heart: a [dwarven] condition on a Dwarf; exerts him at the start of his turns
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a Maneuver ability only
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession on Aragorn, strength +2, damage +1
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally, home 3F, strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwistedTalesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Twisted Tales
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.
		 * Each time a companion or ally loses a skirmish involving an [isengard] Man, you may spot a possession to stack that possession here.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tales");

		assertEquals("Twisted Tales", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeACompanionLosesToAnIsengardManAPossessionMayBeStackedOnTheCopyThatAsks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var heart = scn.GetFreepsCard("heart");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, armor, ring);
		scn.AttachCardsTo(aragorn, sword);

		var tales = scn.GetShadowCard("tales");
		var tales2 = scn.GetShadowCard("tales2");
		var traitor = scn.GetShadowCard("traitor");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(tales, tales2, poison);

		scn.StartGame();
		// Laid after the start of the turn, so that the Heart's start-of-turn exertion has passed.
		scn.AttachCardsTo(gimli, heart);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(heart, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, tales, tales2, poison);
		scn.MoveMinionsToTable(traitor);
		// Fierce only so that the Traitor fights Gimli a second time.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, traitor, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, traitor);

		// 6 against 6: the tie goes to the Shadow side.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(traitor));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales2));
		scn.ShadowChooseOptionalTrigger(tales);

		// Any possession: on the loser, on another companion, or a Shadow one in the support area.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, sword, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, heart, tales2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armor);

		assertInZone(Zone.STACKED, armor);
		assertTrue(scn.GetStackedCards(tales).contains(armor));
		assertEquals(0, scn.GetStackedCards(tales2).size());

		// The other copy's offer, declined: nothing more moves.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetStackedCards(tales).size());
		assertEquals(0, scn.GetStackedCards(tales2).size());
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, poison);

		// The fierce round: Gimli loses to him again, and both copies ask again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, traitor);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales2));
		scn.ShadowChooseOptionalTrigger(tales2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, poison));
		scn.ShadowChooseCard(sword);

		assertTrue(scn.GetStackedCards(tales2).contains(sword));
		assertEquals(1, scn.GetStackedCards(tales).size());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetStackedCards(tales).size());
		assertEquals(1, scn.GetStackedCards(tales2).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyWhoLosesToAnotherIsengardManLetsAShadowPossessionBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var tales = scn.GetShadowCard("tales");
		var agents = scn.GetShadowCard("agents");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(tales, poison);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tales, poison);
		assertAttachedTo(sword, aragorn);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(agents);
		// The move to site 3, Elrond's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, agents);

		// Three Free Peoples cultures, [shire], [elven] and [gondor]: one over 2, so 9 + 2 against 8, and damage +1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(11, scn.GetStrength(agents));
		assertEquals(8, scn.GetStrength(elrond));
		scn.FreepsResolveSkirmish(elrond);
		scn.PassSkirmishActions();

		// The win is a wound and the damage one more.
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tales));
		scn.ShadowAcceptOptionalTrigger();

		// Any possession, whoever's: the Shadow one in the support area or the Free Peoples one on Aragorn.
		assertTrue(scn.ShadowHasCardChoiceAvailable(poison, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tales));
		scn.ShadowChooseCard(poison);

		assertInZone(Zone.STACKED, poison);
		assertTrue(scn.GetStackedCards(tales).contains(poison));
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheCompanionWinsOrLosesToAnIsengardMinionWhoIsNoManOrToAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, armor);

		var tales = scn.GetShadowCard("tales");
		var traitor = scn.GetShadowCard("traitor");
		var guard = scn.GetShadowCard("guard");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(tales);

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, tales);
		scn.MoveMinionsToTable(traitor, guard, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, traitor}, new PhysicalCardImpl[]{gimli, guard},
				new PhysicalCardImpl[]{boromir, brigand});

		// Aragorn (8 + 2) beats the Traitor (6): the [isengard] Man is the one who loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(traitor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tales));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Gimli (6) loses to the Uruk Guard (9): an [isengard] minion who is no Man.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tales));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Boromir (7) loses to the Brigand (8): a Man of another culture.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tales));
		assertEquals(0, scn.GetStackedCards(tales).size());
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoPossessionInPlayItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveCompanionsToTable(gimli);

		var tales = scn.GetShadowCard("tales");
		var traitor = scn.GetShadowCard("traitor");
		scn.MoveCardsToSupportArea(tales);

		scn.StartGame();
		// Laid after the start of the turn, so that the Heart's start-of-turn exertion has passed.
		scn.AttachCardsTo(gimli, heart);
		assertAttachedTo(heart, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, tales);
		scn.MoveMinionsToTable(traitor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, traitor);

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli lost to an [isengard] Man, but nothing in play is a possession: The One Ring is none, and the
		// Dwarven Heart he bears is a condition.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tales));
		assertEquals(0, scn.GetStackedCards(tales).size());
		assertAttachedTo(heart, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
