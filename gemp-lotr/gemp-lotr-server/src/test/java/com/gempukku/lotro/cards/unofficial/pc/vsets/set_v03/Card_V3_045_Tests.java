package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_045_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quartermaster", "103_45");
					put("quartermaster2", "103_45");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text at all
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used here
					put("athelas", "1_94");     // Athelas: Free Peoples possession with no numbers; a fellowship ability
					put("barahir", "6_55");     // Ring of Barahir: Free Peoples artifact with no numbers; an optional start-of-turn trigger, so it goes down after the start
					put("shield", "1_107");     // Great Shield: Free Peoples possession; the minion archery total is -1
					put("mail", "1_101");       // Coat of Mail: Free Peoples possession; bearer is overwhelmed only at triple strength
					put("alliance", "1_49");    // The Last Alliance of Elves and Men: Free Peoples condition on a [gondor] Man; +1 strength for each Elf, and there is none
					put("poison", "8_28");      // Spider Poison: Shadow possession a companion bears; bearer exerts when assigned to a skirmish
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally whose home is site 3, strength 8, vitality 4
					put("vilya", "3_27");       // Vilya: Free Peoples artifact on Elrond, vitality +1; a maneuver ability never used here
					put("pipe", "1_285");       // Bilbo's Pipe: Free Peoples possession on a Hobbit, with no numbers; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairQuartermasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Corsair Quartermaster
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Corsair.</b>
		 * 	At the start of each skirmish involving this minion, the Free Peoples player may hinder any number of
		 * 	Free Peoples items on a character in that skirmish.
		 * 	This minion is <b>damage +1</b> for each Free Peoples item you can spot on each character in its skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quartermaster");

		assertEquals("Corsair Quartermaster", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfItsSkirmishTheFreePeoplesPlayerMayHinderItemsOnTheCompanionItFightsAndEachOneLeftIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var troop = scn.GetShadowCard("troop");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var barahir = scn.GetFreepsCard("barahir");
		var shield = scn.GetFreepsCard("shield");
		var alliance = scn.GetFreepsCard("alliance");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// On Aragorn: a possession and an artifact, which are items; a possession hindered beforehand; a
		// condition, which is no item; a Shadow possession. On Boromir, who fights another minion: a possession.
		scn.AttachCardsTo(aragorn, athelas, shield, alliance, poison);
		scn.AttachCardsTo(boromir, mail);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.HinderCard(shield);
		scn.MoveMinionsToTable(quartermaster, troop);
		scn.SkipToAssignments();

		// Nobody is skirmishing yet, so it has no skirmish and no bonus, whatever the companions bear.
		assertEquals(0, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, quartermaster}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(aragorn);

		// The Free Peoples player is asked, and may choose neither, either or both of the two items.
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, barahir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shield, alliance, poison, mail));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// One for the Athelas and one for the Ring of Barahir.
		assertEquals(2, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		assertFalse(scn.IsHindered(athelas));

		scn.FreepsChooseCard(athelas);

		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(barahir));
		assertFalse(scn.IsHindered(alliance));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(mail));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(shield));
		// Only the Ring of Barahir is still an item to be spotted.
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ChoosingNoItemHindersNothingAndTheLosingCompanionTakesAWoundMoreForEachItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(quartermaster);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, quartermaster);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, barahir));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();

		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(barahir));
		assertEquals(2, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 loses to 12: one wound for losing and two for damage +2.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsAskedAgainInASecondSkirmishOfTheTurnAndOfferedOnlyTheItemsNotYetHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(quartermaster);
		// Fierce, so that it skirmishes twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("Corsair Quartermaster"), null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, quartermaster);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, barahir));
		scn.FreepsChooseCard(athelas);
		assertTrue(scn.IsHindered(athelas));
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));

		// One wound for losing and one for damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, quartermaster);
		scn.FreepsResolveSkirmish(aragorn);

		// Asked again; the Athelas, still hindered, is not offered a second time.
		assertTrue(scn.FreepsHasCardChoiceAvailable(barahir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(athelas));
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));

		scn.FreepsChooseCard(barahir);

		assertTrue(scn.IsHindered(barahir));
		assertTrue(scn.IsHindered(athelas));
		// No item left to spot: no damage bonus at all, and one wound for losing.
		assertFalse(scn.HasKeyword(quartermaster, Keyword.DAMAGE));
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NobodyIsAskedAtTheStartOfAnotherMinionsSkirmishOrOfItsOwnAgainstACompanionWithNoItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas, mail);

		scn.StartGame();
		scn.MoveMinionsToTable(quartermaster, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{boromir, quartermaster});

		// Aragorn, with two items, fights the Troop while the Quartermaster waits for its own skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(mail));
		scn.PassSkirmishActions();

		// The Quartermaster's own skirmish, against Boromir who bears nothing: there is nothing to offer.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(quartermaster));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(mail));
		assertFalse(scn.HasKeyword(quartermaster, Keyword.DAMAGE));
	}

	@Test
	public void TheOneRingOnTheRingBearerIsNotAnItemToHinderOrToCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var pipe = scn.GetFreepsCard("pipe");
		scn.AttachCardsTo(frodo, pipe);

		scn.StartGame();
		scn.MoveMinionsToTable(quartermaster);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, quartermaster);
		scn.FreepsResolveSkirmish(frodo);

		// Frodo bears the Ring and a pipe. The Ring has no side and is neither possession nor artifact: the Free
		// Peoples player is asked about the pipe alone, and the pipe alone is counted.
		assertAttachedTo(ring, frodo);
		assertAttachedTo(pipe, frodo);
		assertTrue(scn.IsCharSkirmishing(quartermaster));
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring));
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));

		scn.FreepsChooseCard(pipe);

		assertTrue(scn.IsHindered(pipe));
		assertFalse(scn.IsHindered(ring));
		// With the pipe hindered the Ring is all that is left on him, and it is worth nothing.
		assertEquals(0, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void HasNoDamageBonusDuringASkirmishItIsNotIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas, mail);

		scn.StartGame();
		scn.MoveMinionsToTable(quartermaster, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{boromir, quartermaster});

		// Aragorn bears two items, but he is in the Troop's skirmish, not the Quartermaster's.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(quartermaster));
		assertEquals(0, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnlyAtTheStartOfItsOwnSkirmishAndOnlyAboutTheItemsInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var quartermaster2 = scn.GetShadowCard("quartermaster2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas = scn.GetFreepsCard("athelas");
		var barahir = scn.GetFreepsCard("barahir");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, athelas);
		scn.AttachCardsTo(boromir, mail);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveMinionsToTable(quartermaster, quartermaster2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, quartermaster}, new PhysicalCardImpl[]{boromir, quartermaster2});

		// Aragorn's skirmish: one offer, of his two items. One is left unhindered, and nobody asks about it again.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, barahir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(mail));
		assertEquals(2, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		scn.FreepsChooseCard(athelas);
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsHindered(barahir));
		assertFalse(scn.IsHindered(mail));
		scn.PassSkirmishActions();

		// Boromir's skirmish: the other copy asks, about his Coat of Mail and not about the Ring of Barahir.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(mail));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barahir, athelas));
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.GetKeywordCount(quartermaster2, Keyword.DAMAGE));
		scn.FreepsDeclineChoosing();
		assertFalse(scn.IsHindered(mail));
		assertFalse(scn.IsHindered(barahir));
		assertEquals(1, scn.GetKeywordCount(quartermaster2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllysItemCountsAndMayBeHinderedWhenTheAllyIsTheCharacterInItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quartermaster = scn.GetShadowCard("quartermaster");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");

		scn.StartGame();
		scn.SkipToSite(2);
		// Elrond's home is site 3, where the fellowship is about to move and where he may be assigned.
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveMinionsToTable(quartermaster);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, quartermaster);
		scn.FreepsResolveSkirmish(elrond);

		assertTrue(scn.FreepsHasCardChoiceAvailable(vilya));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.GetKeywordCount(quartermaster, Keyword.DAMAGE));

		scn.FreepsChooseCard(vilya);

		assertTrue(scn.IsHindered(vilya));
		assertFalse(scn.HasKeyword(quartermaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
