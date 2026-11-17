package com.gempukku.lotro.cards.official.set15;

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

public class Card_15_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "15_173");

					// Minions
					put("ugluk", "15_172");     // Uglúk, Ugly Fellow: [uruk-hai] Uruk-hai, no damage bonus; his trigger needs a skirmish of his
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, hunter 6, no damage bonus, no other text
					put("murderous", "11_195"); // Murderous Uruk: [uruk-hai] Uruk-hai, strength 8, vitality 2, damage +1 and muster
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 only
					put("saruman", "12_144");   // Saruman, Agent of the Dark Lord: an [uruk-hai] Wizard; his skirmish ability and kill trigger are not reached
					put("hillman", "15_90");    // Rapt Hillman: Maneuver: spot another [men] Man and exert this minion to take control of a site
					put("herdsman", "15_83");   // Hunting Herdsman: [men] Man, hunter 1; +3 for each follower (none here)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					// Free Peoples cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf to exert for Baruk Khazad
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only spotted, for Long I Fell
					put("baruk", "5_5");        // Baruk Khazad: Maneuver: exert a Dwarf to wound a minion
					put("baruk2", "5_5");
					put("fell", "4_97");        // Long I Fell: Skirmish: the Shadow player may make you wound a minion of your choice
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UgluksSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uglúk's Sword, Weapon of Command
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [uruk-hai] Uruk-hai.<br>While you control 2 or more sites, bearer can not take wounds (except during skirmish phases).<br>If bearer is Uglúk, he is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Uglúk's Sword", card.getBlueprint().getTitle());
		assertEquals("Weapon of Command", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnlyOnAnUrukHaiUrukHaiAndMakesUglukDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var butcher = scn.GetShadowCard("butcher");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, butcher, guard, saruman);
		scn.SetTwilight(5);
		assertInHand(sword);
		assertEquals(0, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);
		// Two [uruk-hai] Uruk-hai of different titles; not a [sauron] Uruk-hai, nor an [uruk-hai] minion who is
		// no Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ugluk, butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ugluk);

		assertAttachedTo(sword, ugluk);
		assertEquals(1, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnlyIfHeIsUgluk() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var ugluk = scn.GetShadowCard("ugluk");
		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, butcher);
		scn.AttachCardsTo(butcher, sword);

		// Borne by the Butcher, with Uglúk in play beside him: neither is damage +1.
		assertAttachedTo(sword, butcher);
		assertEquals(0, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));

		scn.AttachCardsTo(ugluk, sword);

		// Borne by Uglúk: he is damage +1, and the Butcher is not.
		assertAttachedTo(sword, ugluk);
		assertEquals(1, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingASecondSiteStopsTheBearerTakingWoundsInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		var baruk2 = scn.GetFreepsCard("baruk2");
		scn.MoveCompanionsToTable(gimli);

		var sword = scn.GetShadowCard("sword");
		var butcher = scn.GetShadowCard("butcher");
		var murderous = scn.GetShadowCard("murderous");
		var hillman = scn.GetShadowCard("hillman");
		var herdsman = scn.GetShadowCard("herdsman");

		scn.StartGame();
		// Player 1's third turn: both markers are on site 3, so sites 1 and 2 can be controlled.
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(baruk, baruk2);
		scn.MoveMinionsToTable(butcher, murderous, hillman, herdsman);
		scn.AttachCardsTo(butcher, sword);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertAttachedTo(sword, butcher);
		assertInHand(baruk);
		assertInHand(baruk2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(murderous));
		assertEquals(0, scn.GetWoundsOn(hillman));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// One site: the bearer may be wounded.
		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(butcher, murderous));
		scn.FreepsChooseCard(butcher);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(butcher));
		scn.FreepsPass();

		// The Shadow player takes a second site by the Hillman's ability.
		assertTrue(scn.ShadowActionAvailable(hillman));
		scn.ShadowUseCardAction(hillman);
		assertEquals(1, scn.GetWoundsOn(hillman));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));

		// Two sites: the bearer is not offered; another [uruk-hai] Uruk-hai is, and is wounded.
		scn.FreepsPlayCard(baruk2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(murderous, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(butcher));
		scn.FreepsChooseCard(murderous);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(murderous));
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);

		var sword = scn.GetShadowCard("sword");
		var butcher = scn.GetShadowCard("butcher");
		var murderous = scn.GetShadowCard("murderous");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 the Shadow player's, site 2 the Free Peoples player's (taken on one of player 2's turns).
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToHand(baruk);
		scn.MoveMinionsToTable(butcher, murderous);
		scn.AttachCardsTo(butcher, sword);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.FreepsControls(scn.GetSite(2)));
		assertAttachedTo(sword, butcher);
		assertInHand(baruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(butcher));
		scn.FreepsPlayCard(baruk);
		// Two controlled sites, but one of them is not the Shadow player's: the bearer may be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(butcher, murderous));
		scn.FreepsChooseCard(butcher);
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(murderous));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BearerTakesTheWoundOfASkirmishItLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sword = scn.GetShadowCard("sword");
		var murderous = scn.GetShadowCard("murderous");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(murderous);
		scn.AttachCardsTo(murderous, sword);
		// A strength that only arranges the board: Aragorn 8 + 2 beats the Uruk's 8 + 1.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertAttachedTo(sword, murderous);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, murderous);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(murderous));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(murderous));

		scn.PassSkirmishActions();
		// Two sites, but a skirmish phase: the bearer takes the wound.
		assertEquals(1, scn.GetWoundsOn(murderous));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The Murderous Uruk's muster is offered as the regroup phase starts.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(murderous));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerNotInTheSkirmishTakesAWoundDuringAnothersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var sword = scn.GetShadowCard("sword");
		var butcher = scn.GetShadowCard("butcher");
		var murderous = scn.GetShadowCard("murderous");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(fell);
		scn.MoveMinionsToTable(butcher, murderous, troop);
		scn.AttachCardsTo(butcher, sword);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertAttachedTo(sword, butcher);
		assertInHand(fell);

		scn.SkipToAssignments();
		// Aragorn skirmishes the Troop; the bearer is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(murderous));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// The wound is placed during a skirmish phase, so the bearer is a candidate beside the other minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(butcher, murderous, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(butcher);

		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(murderous));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
