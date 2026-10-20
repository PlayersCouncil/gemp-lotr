package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("night", "4_79");
					// Two Elf companions, each strength 6 and vitality 3, whose texts are about bearing a ranged weapon.
					put("pengedhel", "4_81");   // Pengedhel, Naith Warrior
					put("erethon", "4_65");     // Erethon, Naith Lieutenant
					// Elrond, Lord of Rivendell: an Elf ally whose home is site 3; strength 8, vitality 4.  At the
					// start of the turn he heals allies of site 3, and he has a fellowship ability.
					put("elrond", "1_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8, vitality 4

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("savage2", "1_151");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3; "Damage +1." is its whole text; never in a skirmish here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NightWithoutEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Night Without End
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: Response: If an Elf wins a skirmish, exert that Elf to liberate a site or wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("night");

		assertEquals("Night Without End", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAnElfWinsASkirmishThatElfExertsToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetFreepsCard("night");
		var pengedhel = scn.GetFreepsCard("pengedhel");
		var erethon = scn.GetFreepsCard("erethon");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage1");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(pengedhel, erethon, aragorn);
		scn.MoveCardsToHand(night);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pengedhel, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pengedhel);
		// 6 against 5.
		scn.FreepsPass();
		scn.ShadowPass();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(night));
		assertEquals(0, scn.GetWoundsOn(pengedhel));

		scn.FreepsChooseOptionalTrigger(night);

		// The Elf who won exerts, and not the Elf beside him.  The event costs 1.
		assertEquals(1, scn.GetWoundsOn(pengedhel));
		assertEquals(0, scn.GetWoundsOn(erethon));
		// No site is controlled, so there is nothing to liberate and no choice between the two: a minion is
		// wounded.  Any minion: the one just beaten or one that was in no skirmish; not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, party));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, erethon, pengedhel));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(party);

		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(night);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MayLiberateTheHighestSiteAnOpponentControlsInsteadOfWoundingAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetFreepsCard("night");
		var pengedhel = scn.GetFreepsCard("pengedhel");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(pengedhel);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.MoveCardsToHand(night);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pengedhel, savage);
		scn.FreepsResolveSkirmish(pengedhel);
		scn.FreepsPass();
		scn.ShadowPass();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(night));

		scn.FreepsChooseOptionalTrigger(night);
		assertEquals(1, scn.GetWoundsOn(pengedhel));

		// A site to liberate and a minion to wound: the player chooses.
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Wound a minion"));
		scn.FreepsChoose("Liberate a site");

		// The site with the highest number that an opponent controls, by rule: site 2, and not site 1.
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(night);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheBeatenMinionStillInPlayTheChoiceIsOfferedAndTheSiteMayBeLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetFreepsCard("night");
		var pengedhel = scn.GetFreepsCard("pengedhel");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pengedhel);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToHand(night);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pengedhel, runner);
		scn.FreepsResolveSkirmish(pengedhel);
		// 6 against 5.  The response to the win comes before the Runner's wound for losing (engine pass 54;
		// until then the Runner was dead, no minion was left to wound, and the site was liberated unasked).
		scn.FreepsPass();
		scn.ShadowPass();

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(night));

		scn.FreepsChooseOptionalTrigger(night);
		assertTrue(scn.FreepsChoiceAvailable("Wound a minion"));
		scn.FreepsChooseOption("Liberate a site");

		assertEquals(1, scn.GetWoundsOn(pengedhel));
		assertFalse(scn.IsSiteControlled(site1));
		assertInDiscard(night);
		// Then the Runner, vitality 1, is killed by its wound for losing.
		assertInDiscard(runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfAllyWhoWinsASkirmishAtHisHomeSiteMayExertTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetFreepsCard("night");
		var elrond = scn.GetFreepsCard("elrond");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(night);
		// The fellowship moves to site 3, Elrond's home, where he may be assigned to a skirmish.
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, savage);
		scn.FreepsResolveSkirmish(elrond);
		// 8 against 5.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(night));
		assertEquals(0, scn.GetWoundsOn(elrond));

		scn.FreepsChooseOptionalTrigger(night);

		// The Savage is the only minion and is wounded without a choice.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(night);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionWhoIsNoElfWinsOrAnElfLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetFreepsCard("night");
		var pengedhel = scn.GetFreepsCard("pengedhel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pengedhel, aragorn);
		scn.MoveCardsToHand(night);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		// The Runner at 8, to beat an Elf of strength 6 with no damage bonus and without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{pengedhel, runner});

		// Aragorn's 8 against the Savage's 5: a Man wins beside an Elf who could exert, and the event is not
		// offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Pengedhel's 6 against the Runner's 8: he loses and takes a wound.  With vitality 3 he could exert,
		// and the event is not offered.
		scn.FreepsResolveSkirmish(pengedhel);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(pengedhel));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInHand(night);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheElfWhoWonCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetFreepsCard("night");
		var pengedhel = scn.GetFreepsCard("pengedhel");
		var erethon = scn.GetFreepsCard("erethon");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(pengedhel, erethon);
		scn.MoveCardsToHand(night);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Pengedhel cannot exert.  Erethon can.
		scn.AddWoundsToChar(pengedhel, 2);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pengedhel, savage1}, new PhysicalCardImpl[]{erethon, savage2});

		// Pengedhel wins, 6 against 5, and the event is not offered: it is that Elf who must exert, not another.
		scn.FreepsResolveSkirmish(pengedhel);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(erethon));

		// Erethon wins on the same board, 6 against 5, and it is.
		scn.FreepsResolveSkirmish(erethon);
		scn.FreepsPass();
		scn.ShadowPass();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(night));

		scn.FreepsChooseOptionalTrigger(night);
		assertEquals(1, scn.GetWoundsOn(erethon));
		assertEquals(2, scn.GetWoundsOn(pengedhel));
		scn.FreepsChooseCard(savage1);
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInDiscard(night);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
