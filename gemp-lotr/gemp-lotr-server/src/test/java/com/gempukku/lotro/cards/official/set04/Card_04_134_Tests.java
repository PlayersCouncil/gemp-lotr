package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_134");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3, no damage bonus; a
					// skirmish ability used only on purpose
					put("boromir", "1_97");
					// Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					put("knight", "5_35");
					// Gondorian Merchant: [gondor] Man ally, home 3T; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] Hobbit companion; cheated in past his "To play" requirement, and
					// his skirmish ability is used only on purpose
					put("pippin", "103_31");
					// Household Guard: a [rohan] Man companion; Valiant is its whole text
					put("guard", "5_83");
					// Dunlending Footmen: [dunland] Man, site 3 (not roaming at site 3), strength 11, vitality 2; its
					// text is about its own cost
					put("footmen", "6_3");
					// Uruk Savage: [isengard] Uruk-hai, no Man, site 5 (roaming below it), strength 5, vitality 3;
					// Damage +1 is its whole text
					put("savage", "1_151");
					// Gollum, Stinker: minion of no race, site 3 (not roaming at site 3), strength 5 (+1 for each
					// burden: none here), vitality 4; his trigger needs him to win
					put("gollum", "5_25");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_134");
					// Gondorian Merchant: [gondor] Man ally, strength 6, vitality 1, home 3T, so he may be assigned at
					// site 3 of this path; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Uruk Savage: [isengard] Uruk-hai, site 5 (roaming at site 3), strength 5; Damage +1 is its whole
					// text
					put("savage", "1_151");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Sword of Gondor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [gondor] Man.<br>While skirmishing a Man or a roaming minion, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Sword of Gondor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnAGondorManCompanionOrAllyAndOnNoHobbitOfGondorOrManOfRohan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(boromir, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertInZone(Zone.HAND, sword);
		assertTrue(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(sword);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(sword, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerSkirmishingAManIsDamagePlusOneOnlyOnceHisOwnSkirmishBeginsAndNoOtherGondorManIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var footmen = scn.GetShadowCard("footmen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir, knight);
		scn.AttachCardsTo(boromir, sword);

		scn.StartGame();
		assertAttachedTo(sword, boromir);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(footmen, savage);
		// The fellowship moves to site 3.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, footmen},
				new PhysicalCardImpl[]{knight, savage}
		);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(footmen, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		// The Knight, a [gondor] Man skirmishing a roaming minion, bears no sword; the bearer waits for a Man.
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);

		// A Man who is not roaming.
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerWhoBeatsARoamingMinionThatIsNoManPlacesTwoWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(sword, boromir);
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, savage);

		// Site 2: the Savage, of site 5, is roaming.
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 9 against 5, short of double: one wound for the win and one for damage +1
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		// The skirmish is over, and so is the bonus.
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerSkirmishingAMinionThatIsNeitherAManNorRoamingIsNoDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var boromir = scn.GetFreepsCard("boromir");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, sword);

		scn.StartGame();
		assertAttachedTo(sword, boromir);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, gollum);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 9 against 5: the win's one wound and no more
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyBearingItAtHisHomeSiteIsDamagePlusOneSkirmishingARoamingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var sword = scn.GetFreepsCard("sword");
		var merchant = scn.GetFreepsCard("merchant");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, sword);

		scn.StartGame();
		assertAttachedTo(sword, merchant);
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		// The fellowship moves to site 3, the Merchant's home.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merchant, savage);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(1, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
